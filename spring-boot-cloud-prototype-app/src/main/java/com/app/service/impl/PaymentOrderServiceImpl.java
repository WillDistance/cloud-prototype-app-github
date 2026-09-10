package com.app.service.impl;

import com.app.config.PayPalProperties;
import com.app.enums.CurrencyEnum;
import com.app.enums.EntitlementSourceTypeEnum;
import com.app.enums.EntitlementStatusEnum;
import com.app.enums.ErrorCodeEnum;
import com.app.enums.PaymentCallbackProcessStatusEnum;
import com.app.enums.PaymentChannelEnum;
import com.app.enums.PaymentOrderStatusEnum;
import com.app.exception.BusinessException;
import com.app.exception.PaymentWebhookException;
import com.app.mapper.PaymentCallbackMapper;
import com.app.mapper.PaymentOrderMapper;
import com.app.mapper.StorageEntitlementMapper;
import com.app.mapper.StoragePlanMapper;
import com.app.mapper.UserMapper;
import com.app.mapper.UserStorageAccountMapper;
import com.app.pojo.dto.CapturePaymentOrderRequest;
import com.app.pojo.dto.CreatePaymentOrderRequest;
import com.app.pojo.entity.PaymentCallbackEntity;
import com.app.pojo.entity.PaymentOrderEntity;
import com.app.pojo.entity.StorageEntitlementEntity;
import com.app.pojo.entity.StoragePlanEntity;
import com.app.pojo.entity.UserEntity;
import com.app.pojo.entity.UserStorageAccountEntity;
import com.app.pojo.vo.PaymentOrderVo;
import com.app.service.PaymentOrderService;
import com.app.support.payment.paypal.PayPalGateway;
import com.app.utils.BusinessNumberGenerator;
import com.app.utils.UserContextHolderUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * PayPal支付订单实现。
 */
@Slf4j
@Service
public class PaymentOrderServiceImpl implements PaymentOrderService {
    private static final long MAX_WEBHOOK_BYTES = 1024 * 1024;
    private static final Set<String> PAYMENT_COMPLETED_STATUSES = Set.of(
            PaymentOrderStatusEnum.PENDING.getValue(), PaymentOrderStatusEnum.PAID.getValue());

    @Autowired
    private PayPalProperties payPalProperties;
    @Autowired
    private PayPalGateway payPalGateway;
    @Autowired
    private PaymentOrderMapper paymentOrderMapper;
    @Autowired
    private PaymentCallbackMapper paymentCallbackMapper;
    @Autowired
    private StoragePlanMapper storagePlanMapper;
    @Autowired
    private StorageEntitlementMapper entitlementMapper;
    @Autowired
    private UserStorageAccountMapper storageAccountMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public PaymentOrderVo createOrder(CreatePaymentOrderRequest request) {
        requirePayPalConfiguration();
        Long userId = UserContextHolderUtil.getUserId();
        PaymentOrderEntity order = paymentOrderMapper.selectByUserAndClientRequestId(userId, request.getClientRequestId());
        if (order == null) {
            order = createLocalOrder(userId, request);
        }
        if (order.getProviderOrderId() == null || order.getProviderOrderId().isBlank()) {
            if (!PaymentOrderStatusEnum.PENDING.getValue().equals(order.getStatus())) {
                throw new BusinessException(ErrorCodeEnum.PAYMENT_ORDER_STATUS_INVALID);
            }
            try {
                String returnUrl = appendQuery(appendQuery(payPalProperties.getReturnUrl(), "payment", "return"),
                        "orderNo", order.getOrderNo());
                String cancelUrl = appendQuery(appendQuery(payPalProperties.getCancelUrl(), "payment", "cancel"),
                        "orderNo", order.getOrderNo());
                PayPalGateway.PayPalOrderResult result = payPalGateway.createOrder(order.getOrderNo(),
                        order.getPlanNameSnapshot(), order.getAmountCent(), order.getCurrency(), returnUrl, cancelUrl);
                paymentOrderMapper.updateProviderOrder(order.getId(), result.providerOrderId(), result.checkoutUrl());
                order.setProviderOrderId(result.providerOrderId()).setCheckoutUrl(result.checkoutUrl());
            } catch (PayPalGateway.PayPalGatewayException exception) {
                log.warn("PayPal order creation failed orderNo={}", order.getOrderNo());
                throw new BusinessException(ErrorCodeEnum.PAYMENT_GATEWAY_UNAVAILABLE);
            }
        }
        return toVo(order);
    }

    @Override
    public PaymentOrderVo captureOrder(CapturePaymentOrderRequest request) {
        requirePayPalConfiguration();
        PaymentOrderEntity order = paymentOrderMapper.selectByUserAndOrderNo(UserContextHolderUtil.getUserId(), request.getOrderNo());
        if (order == null) {
            throw new BusinessException(ErrorCodeEnum.PAYMENT_ORDER_NOT_FOUND);
        }
        if (PaymentOrderStatusEnum.PAID.getValue().equals(order.getStatus())) {
            return toVo(order);
        }
        if (!PaymentOrderStatusEnum.PENDING.getValue().equals(order.getStatus())) {
            throw new BusinessException(ErrorCodeEnum.PAYMENT_ORDER_STATUS_INVALID);
        }
        if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
            paymentOrderMapper.updateStatusById(order.getId(), PaymentOrderStatusEnum.CLOSED.getValue(),
                    PaymentOrderStatusEnum.PENDING.getValue());
            throw new BusinessException(ErrorCodeEnum.PAYMENT_ORDER_STATUS_INVALID);
        }
        if (order.getProviderOrderId() == null || order.getProviderOrderId().isBlank()) {
            throw new BusinessException(ErrorCodeEnum.PAYMENT_PROVIDER_ORDER_INVALID);
        }
        try {
            PayPalGateway.PayPalCaptureResult result = payPalGateway.captureOrder(order.getProviderOrderId(), order.getOrderNo());
            if (result.providerCaptureId() != null && !result.providerCaptureId().isBlank()) {
                paymentOrderMapper.updateProviderCapture(order.getId(), result.providerCaptureId());
                order.setProviderCaptureId(result.providerCaptureId());
            }
            return toVo(order);
        } catch (PayPalGateway.PayPalGatewayException exception) {
            log.warn("PayPal capture failed orderNo={}", order.getOrderNo());
            throw new BusinessException(ErrorCodeEnum.PAYMENT_GATEWAY_UNAVAILABLE);
        }
    }

    @Override
    public PaymentOrderVo getOrder(String orderNo) {
        PaymentOrderEntity order = paymentOrderMapper.selectByUserAndOrderNo(UserContextHolderUtil.getUserId(), orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCodeEnum.PAYMENT_ORDER_NOT_FOUND);
        }
        return toVo(order);
    }

    @Override
    public void handlePayPalWebhook(String rawPayload, Map<String, String> headers) {
        if (rawPayload == null || rawPayload.length() > MAX_WEBHOOK_BYTES) {
            throw new PaymentWebhookException("Webhook payload is invalid", true);
        }
        PayPalGateway.PayPalWebhookEvent event;
        try {
            event = payPalGateway.verifyWebhook(rawPayload, headers.get("paypal-transmission-id"),
                    headers.get("paypal-transmission-time"), headers.get("paypal-cert-url"),
                    headers.get("paypal-auth-algo"), headers.get("paypal-transmission-sig"));
        } catch (PayPalGateway.PayPalGatewayException exception) {
            throw new PaymentWebhookException("Webhook verification service unavailable", exception, false);
        }
        if (!event.signatureVerified()) {
            saveRejectedCallback(event, "Webhook signature verification failed");
            throw new PaymentWebhookException("Webhook signature is invalid", true);
        }
        if (event.eventId() == null || event.eventId().isBlank()) {
            throw new PaymentWebhookException("Webhook event id is missing", true);
        }
        transactionTemplate.executeWithoutResult(status -> processVerifiedWebhook(event));
    }

    private void processVerifiedWebhook(PayPalGateway.PayPalWebhookEvent event) {
        if (paymentCallbackMapper.selectByChannelAndEventId(PaymentChannelEnum.PAYPAL.getValue(), event.eventId()) != null) {
            return;
        }
        PaymentCallbackEntity callback = new PaymentCallbackEntity()
                .setId(IdWorker.getId())
                .setOrderNo(event.orderNo())
                .setPaymentChannel(PaymentChannelEnum.PAYPAL.getValue())
                .setChannelTransactionNo(event.providerCaptureId())
                .setCallbackEventId(event.eventId())
                .setRawPayload(event.rawPayload())
                .setSignatureValue(event.signatureValue())
                .setSignatureVerified(1)
                .setCallbackAmountCent(event.amountCent())
                .setCallbackCurrency(event.currency())
                .setCallbackStatus(event.providerStatus())
                .setProcessStatus(PaymentCallbackProcessStatusEnum.RECEIVED.getValue())
                .setReceivedTime(LocalDateTime.now());
        paymentCallbackMapper.insert(callback);

        if (!payPalGateway.isPaymentCompleted(event)) {
            completeCallback(callback, PaymentCallbackProcessStatusEnum.SUCCESS.getValue(), null);
            return;
        }

        PaymentOrderEntity order = findOrder(event);
        if (order == null) {
            completeCallback(callback, PaymentCallbackProcessStatusEnum.REJECTED.getValue(), "Order not found");
            return;
        }
        order = paymentOrderMapper.selectByIdForUpdate(order.getId());
        if (order == null) {
            completeCallback(callback, PaymentCallbackProcessStatusEnum.REJECTED.getValue(), "Order not found");
            return;
        }
        callback.setPaymentOrderId(order.getId());
        if (event.providerOrderId() != null && !event.providerOrderId().equals(order.getProviderOrderId())) {
            completeCallback(callback, PaymentCallbackProcessStatusEnum.REJECTED.getValue(), "Provider order mismatch");
            return;
        }
        if (event.amountCent() == null || !event.amountCent().equals(order.getAmountCent())
                || event.currency() == null || !event.currency().equalsIgnoreCase(order.getCurrency())) {
            completeCallback(callback, PaymentCallbackProcessStatusEnum.REJECTED.getValue(), "Payment amount or currency mismatch");
            return;
        }
        if (!PAYMENT_COMPLETED_STATUSES.contains(order.getStatus())) {
            completeCallback(callback, PaymentCallbackProcessStatusEnum.REJECTED.getValue(), "Order status is not payable");
            return;
        }
        if (PaymentOrderStatusEnum.PAID.getValue().equals(order.getStatus())) {
            completeCallback(callback, PaymentCallbackProcessStatusEnum.DUPLICATE.getValue(), null);
            return;
        }

        UserEntity user = userMapper.selectById(order.getUserId());
        if (user == null) {
            completeCallback(callback, PaymentCallbackProcessStatusEnum.REJECTED.getValue(), "User not found");
            return;
        }
        ensureStorageAccount(order.getUserId());
        LocalDateTime now = LocalDateTime.now();
        StorageEntitlementEntity entitlement = new StorageEntitlementEntity()
                .setId(IdWorker.getId())
                .setEntitlementNo(BusinessNumberGenerator.generate("ENT"))
                .setUserId(order.getUserId())
                .setSourceType(EntitlementSourceTypeEnum.PURCHASE.getValue())
                .setPaymentOrderId(order.getId())
                .setNameSnapshot(order.getPlanNameSnapshot())
                .setCapacityBytes(order.getCapacityBytesSnapshot())
                .setDurationValue(order.getDurationValueSnapshot())
                .setDurationUnit(order.getDurationUnitSnapshot())
                .setUserTimeZoneSnapshot(user.getTimeZone() == null || user.getTimeZone().isBlank() ? "UTC" : user.getTimeZone())
                .setEffectiveTime(now)
                .setExpireTime(calculateExpireTime(now, user.getTimeZone(), order.getDurationValueSnapshot(),
                        order.getDurationUnitSnapshot()))
                .setStatus(EntitlementStatusEnum.ACTIVE.getValue());
        entitlementMapper.insert(entitlement);
        int updated = paymentOrderMapper.markPaid(order.getId(), event.providerCaptureId(), now);
        if (updated != 1) {
            throw new PaymentWebhookException("Payment order state changed during webhook processing", false);
        }
        completeCallback(callback, PaymentCallbackProcessStatusEnum.SUCCESS.getValue(), null);
    }

    private PaymentOrderEntity createLocalOrder(Long userId, CreatePaymentOrderRequest request) {
        Integer version = request.getPlanVersion();
        if (version == null) {
            throw new BusinessException(ErrorCodeEnum.PAYMENT_PLAN_INVALID);
        }
        StoragePlanEntity plan = storagePlanMapper.selectActiveByCodeAndVersion(request.getPlanCode(), version);
        LocalDateTime now = LocalDateTime.now();
        if (plan == null || plan.getEffectiveTime() != null && plan.getEffectiveTime().isAfter(now)
                || plan.getPriceCent() == null || plan.getPriceCent() <= 0) {
            throw new BusinessException(ErrorCodeEnum.PAYMENT_PLAN_INVALID);
        }
        String currency = plan.getCurrency() == null ? null : plan.getCurrency().toUpperCase(Locale.ROOT);
        if (currency == null || !payPalProperties.getSupportedCurrencies().contains(currency)) {
            throw new BusinessException(ErrorCodeEnum.PAYMENT_CURRENCY_UNSUPPORTED);
        }
        PaymentOrderEntity order = new PaymentOrderEntity()
                .setId(IdWorker.getId())
                .setOrderNo(BusinessNumberGenerator.generate("ORD"))
                .setUserId(userId)
                .setStoragePlanId(plan.getId())
                .setPlanCodeSnapshot(plan.getPlanCode())
                .setPlanVersionSnapshot(plan.getPlanVersion())
                .setPlanNameSnapshot(plan.getPlanName())
                .setCapacityBytesSnapshot(plan.getCapacityBytes())
                .setDurationValueSnapshot(plan.getDurationValue())
                .setDurationUnitSnapshot(plan.getDurationUnit())
                .setAmountCent(plan.getPriceCent())
                .setCurrency(currency)
                .setPaymentChannel(PaymentChannelEnum.PAYPAL.getValue())
                .setStatus(PaymentOrderStatusEnum.PENDING.getValue())
                .setExpireTime(now.plusMinutes(payPalProperties.getOrderExpireMinutes()))
                .setClientRequestId(request.getClientRequestId());
        paymentOrderMapper.insert(order);
        return order;
    }

    private PaymentOrderEntity findOrder(PayPalGateway.PayPalWebhookEvent event) {
        PaymentOrderEntity order = event.providerOrderId() == null ? null
                : paymentOrderMapper.selectByProviderOrderId(event.providerOrderId());
        if (order == null && event.orderNo() != null) {
            order = paymentOrderMapper.selectByOrderNo(event.orderNo());
        }
        return order;
    }

    private void ensureStorageAccount(Long userId) {
        UserStorageAccountEntity account = storageAccountMapper.selectByUserIdForUpdate(userId);
        if (account == null) {
            storageAccountMapper.insert(new UserStorageAccountEntity().setId(IdWorker.getId()).setUserId(userId)
                    .setUsedBytes(0L).setReservedBytes(0L).setLockVersion(0L));
        }
    }

    private void saveRejectedCallback(PayPalGateway.PayPalWebhookEvent event, String reason) {
        if (event.eventId() == null || event.eventId().isBlank()
                || paymentCallbackMapper.selectByChannelAndEventId(PaymentChannelEnum.PAYPAL.getValue(), event.eventId()) != null) {
            return;
        }
        PaymentCallbackEntity callback = new PaymentCallbackEntity()
                .setId(IdWorker.getId())
                .setOrderNo(event.orderNo())
                .setPaymentChannel(PaymentChannelEnum.PAYPAL.getValue())
                .setChannelTransactionNo(event.providerCaptureId())
                .setCallbackEventId(event.eventId())
                .setRawPayload(event.rawPayload())
                .setSignatureValue(event.signatureValue())
                .setSignatureVerified(0)
                .setCallbackAmountCent(event.amountCent())
                .setCallbackCurrency(event.currency())
                .setCallbackStatus(event.providerStatus())
                .setProcessStatus(PaymentCallbackProcessStatusEnum.REJECTED.getValue())
                .setFailureReason(reason)
                .setReceivedTime(LocalDateTime.now())
                .setProcessedTime(LocalDateTime.now());
        paymentCallbackMapper.insert(callback);
    }

    private void completeCallback(PaymentCallbackEntity callback, String processStatus, String reason) {
        callback.setProcessStatus(processStatus).setFailureReason(reason).setProcessedTime(LocalDateTime.now());
        paymentCallbackMapper.updateById(callback);
    }

    private LocalDateTime calculateExpireTime(LocalDateTime effectiveTime, String timeZone,
                                              Integer durationValue, String durationUnit) {
        ZoneId zone = ZoneId.of(timeZone == null || timeZone.isBlank() ? "UTC" : timeZone);
        LocalDate localDate = effectiveTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(zone).toLocalDate();
        LocalDateTime localExpire = switch (durationUnit) {
            case "DAY" -> localDate.plusDays(durationValue).atStartOfDay();
            case "MONTH" -> localDate.plusMonths(durationValue).atStartOfDay();
            case "YEAR" -> localDate.plusYears(durationValue).atStartOfDay();
            default -> throw new BusinessException(ErrorCodeEnum.PAYMENT_PLAN_INVALID);
        };
        return localExpire.atZone(zone).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
    }

    private void requirePayPalConfiguration() {
        if (!payPalProperties.isEnabled() || payPalProperties.getReturnUrl() == null
                || payPalProperties.getCancelUrl() == null) {
            throw new BusinessException(ErrorCodeEnum.PAYMENT_GATEWAY_UNAVAILABLE);
        }
    }

    private String appendQuery(String base, String key, String value) {
        if (base == null || base.isBlank()) {
            throw new BusinessException(ErrorCodeEnum.PAYMENT_GATEWAY_UNAVAILABLE);
        }
        return base + (base.contains("?") ? "&" : "?") + key + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private PaymentOrderVo toVo(PaymentOrderEntity order) {
        return new PaymentOrderVo(order.getOrderNo(), order.getStatus(), order.getPaymentChannel(),
                order.getPaymentMethod(), order.getAmountCent(), order.getCurrency(), order.getProviderOrderId(),
                order.getCheckoutUrl(), order.getExpireTime(), order.getPaidTime());
    }
}
