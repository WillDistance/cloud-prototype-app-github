package com.app.service.impl;

import com.app.enums.ErrorCodeEnum;
import com.app.enums.PaymentChannelEnum;
import com.app.enums.PaymentOrderStatusEnum;
import com.app.enums.StoragePlanStatusEnum;
import com.app.exception.BusinessException;
import com.app.mapper.PaymentOrderMapper;
import com.app.mapper.StoragePlanMapper;
import com.app.payment.PaymentInitiation;
import com.app.payment.PaymentInitiator;
import com.app.pojo.dto.CreatePaymentOrderDto;
import com.app.pojo.entity.PaymentOrderEntity;
import com.app.pojo.entity.StoragePlanEntity;
import com.app.pojo.vo.PaymentOrderVo;
import com.app.security.AuthenticatedUser;
import com.app.security.UserContextHolder;
import com.app.service.PaymentOrderService;
import com.app.utils.BusinessNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * 支付订单服务实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Service
public class PaymentOrderServiceImpl implements PaymentOrderService {
    private final StoragePlanMapper planMapper;
    private final PaymentOrderMapper orderMapper;
    private final PaymentInitiator paymentInitiator;
    private final BusinessNumberGenerator numberGenerator;
    private final Clock clock;
    private final Duration paymentExpireDuration;

    @Autowired
    public PaymentOrderServiceImpl(StoragePlanMapper planMapper, PaymentOrderMapper orderMapper,
                                   PaymentInitiator paymentInitiator,
                                   @Value("${payment.order.expire-minutes:30}") long expireMinutes) {
        this(planMapper, orderMapper, paymentInitiator, new BusinessNumberGenerator(), Clock.systemUTC(),
                Duration.ofMinutes(expireMinutes));
    }

    public PaymentOrderServiceImpl(StoragePlanMapper planMapper, PaymentOrderMapper orderMapper,
                                   PaymentInitiator paymentInitiator, BusinessNumberGenerator numberGenerator,
                                   Clock clock, Duration paymentExpireDuration) {
        this.planMapper = planMapper;
        this.orderMapper = orderMapper;
        this.paymentInitiator = paymentInitiator;
        this.numberGenerator = numberGenerator;
        this.clock = clock;
        this.paymentExpireDuration = paymentExpireDuration;
    }

    @Override
    @Transactional
    public PaymentOrderVo createOrder(CreatePaymentOrderDto dto) {
        Long userId = currentUserId();
        PaymentOrderEntity existing = orderMapper.selectByUserAndClientRequestId(userId, dto.getClientRequestId());
        if (existing != null) {
            return toVo(existing, paymentInitiator.initiate(existing));
        }

        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        StoragePlanEntity plan = planMapper.selectActivePlan(dto.getPlanCode(), dto.getPlanVersion(), now);
        if (!isEffectiveActivePlan(plan, now)) {
            throw new BusinessException(ErrorCodeEnum.PAYMENT_PLAN_INVALID);
        }

        PaymentOrderEntity order = snapshotOrder(userId, dto.getClientRequestId(), plan, now);
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException exception) {
            PaymentOrderEntity concurrent = orderMapper.selectByUserAndClientRequestId(userId,
                    dto.getClientRequestId());
            if (concurrent != null) {
                return toVo(concurrent, paymentInitiator.initiate(concurrent));
            }
            throw exception;
        }
        return toVo(order, paymentInitiator.initiate(order));
    }

    @Override
    public PaymentOrderVo getOrder(String orderNo) {
        PaymentOrderEntity order = orderMapper.selectByUserAndOrderNo(currentUserId(), orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCodeEnum.PAYMENT_ORDER_NOT_FOUND);
        }
        return toVo(order, null);
    }

    /**
     * 根据套餐和用户信息创建支付订单快照。
     *
     * @param userId          用户ID
     * @param clientRequestId 客户端幂等请求号
     * @param plan            存储套餐
     * @param now             当前UTC时间
     * @return 方法处理后的结果
     */
    private PaymentOrderEntity snapshotOrder(Long userId, String clientRequestId, StoragePlanEntity plan,
                                             LocalDateTime now) {
        return new PaymentOrderEntity().setOrderNo(numberGenerator.generate("PO")).setUserId(userId)
                .setStoragePlanId(plan.getId()).setPlanCodeSnapshot(plan.getPlanCode())
                .setPlanVersionSnapshot(plan.getPlanVersion()).setPlanNameSnapshot(plan.getPlanName())
                .setCapacityBytesSnapshot(plan.getCapacityBytes()).setDurationValueSnapshot(plan.getDurationValue())
                .setDurationUnitSnapshot(plan.getDurationUnit()).setAmountCent(plan.getPriceCent())
                .setCurrency(plan.getCurrency()).setPaymentChannel(PaymentChannelEnum.PINGPONG)
                .setStatus(PaymentOrderStatusEnum.PENDING).setExpireTime(now.plus(paymentExpireDuration))
                .setClientRequestId(clientRequestId);
    }

    /**
     * 判断存储套餐在指定时间是否有效且在售。
     *
     * @param plan 存储套餐
     * @param now  当前UTC时间
     * @return 操作是否成功
     */
    private boolean isEffectiveActivePlan(StoragePlanEntity plan, LocalDateTime now) {
        return plan != null && plan.getStatus() == StoragePlanStatusEnum.ACTIVE && plan.getEffectiveTime() != null
                && !plan.getEffectiveTime().isAfter(now);
    }

    /**
     * 将业务实体转换为前端视图对象。
     *
     * @param entity     数据库实体
     * @param initiation 支付发起结果
     * @return 方法处理后的结果
     */
    private PaymentOrderVo toVo(PaymentOrderEntity entity, PaymentInitiation initiation) {
        PaymentOrderVo vo = new PaymentOrderVo().setOrderNo(entity.getOrderNo())
                .setPlanCodeSnapshot(entity.getPlanCodeSnapshot())
                .setPlanVersionSnapshot(entity.getPlanVersionSnapshot())
                .setPlanNameSnapshot(entity.getPlanNameSnapshot())
                .setCapacityBytesSnapshot(entity.getCapacityBytesSnapshot())
                .setDurationValueSnapshot(entity.getDurationValueSnapshot())
                .setDurationUnitSnapshot(value(entity.getDurationUnitSnapshot()))
                .setAmountCent(entity.getAmountCent()).setCurrency(value(entity.getCurrency()))
                .setPaymentChannel(value(entity.getPaymentChannel())).setStatus(value(entity.getStatus()))
                .setExpireTime(entity.getExpireTime()).setPaidTime(entity.getPaidTime())
                .setClosedTime(entity.getClosedTime()).setClientRequestId(entity.getClientRequestId());
        if (initiation != null) {
            vo.setPaymentRedirectUrl(initiation.redirectUrl())
                    .setPaymentParameters(initiation.parameters() == null ? Map.of() : initiation.parameters());
        }
        return vo;
    }

    /**
     * 获取枚举值对应的持久化字符串。
     *
     * @param value 权益时长数值
     * @return 方法处理后的结果
     */
    private String value(com.app.enums.DurationUnitEnum value) {
        return value == null ? null : value.getValue();
    }

    /**
     * 获取枚举值对应的持久化字符串。
     *
     * @param value 权益时长数值
     * @return 方法处理后的结果
     */
    private String value(com.app.enums.CurrencyEnum value) {
        return value == null ? null : value.getValue();
    }

    /**
     * 获取枚举值对应的持久化字符串。
     *
     * @param value 权益时长数值
     * @return 方法处理后的结果
     */
    private String value(PaymentChannelEnum value) {
        return value == null ? null : value.getValue();
    }

    /**
     * 获取枚举值对应的持久化字符串。
     *
     * @param value 权益时长数值
     * @return 方法处理后的结果
     */
    private String value(PaymentOrderStatusEnum value) {
        return value == null ? null : value.getValue();
    }

    /**
     * 获取当前认证用户ID。
     *
     * @return 方法处理后的结果
     */
    private Long currentUserId() {
        AuthenticatedUser user = UserContextHolder.get();
        if (user == null || user.userId() == null) {
            throw new BusinessException(ErrorCodeEnum.AUTH_REQUIRED);
        }
        return user.userId();
    }
}
