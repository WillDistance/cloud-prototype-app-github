package com.app.service;

import com.app.enums.*;
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
import com.app.service.impl.PaymentOrderServiceImpl;
import com.app.utils.BusinessNumberGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

import java.time.*;
import java.util.Map;

import static com.app.enums.ErrorCodeEnum.PAYMENT_ORDER_NOT_FOUND;
import static com.app.enums.ErrorCodeEnum.PAYMENT_PLAN_INVALID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentOrderServiceImplTest {
    private static final Instant NOW = Instant.parse("2026-09-06T12:00:00Z");

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void shouldCreateOrderWithCompletePlanSnapshotExpiryAndPaymentInitiation() {
        StoragePlanMapper planMapper = mock(StoragePlanMapper.class);
        PaymentOrderMapper orderMapper = mock(PaymentOrderMapper.class);
        PaymentInitiator initiator = mock(PaymentInitiator.class);
        BusinessNumberGenerator generator = mock(BusinessNumberGenerator.class);
        when(generator.generate("PO")).thenReturn("PO20260906120000000ABCDEF123456");
        when(planMapper.selectActivePlan("BASIC", 3, utcNow())).thenReturn(plan());
        when(initiator.initiate(any())).thenReturn(new PaymentInitiation(
                "https://pay.example/launch/token", Map.of("token", "safe-token")));
        UserContextHolder.set(new AuthenticatedUser(7L, "Asia/Shanghai", "token"));
        PaymentOrderServiceImpl service = service(planMapper, orderMapper, initiator, generator);

        PaymentOrderVo result = service.createOrder(new CreatePaymentOrderDto()
                .setPlanCode("BASIC").setPlanVersion(3).setClientRequestId("req-001"));

        assertEquals("PO20260906120000000ABCDEF123456", result.getOrderNo());
        assertEquals("BASIC", result.getPlanCodeSnapshot());
        assertEquals(3, result.getPlanVersionSnapshot());
        assertEquals("基础套餐", result.getPlanNameSnapshot());
        assertEquals(107374182400L, result.getCapacityBytesSnapshot());
        assertEquals(12, result.getDurationValueSnapshot());
        assertEquals("MONTH", result.getDurationUnitSnapshot());
        assertEquals(9900L, result.getAmountCent());
        assertEquals("CNY", result.getCurrency());
        assertEquals("PINGPONG", result.getPaymentChannel());
        assertEquals("PENDING", result.getStatus());
        assertEquals(LocalDateTime.of(2026, 9, 6, 12, 30), result.getExpireTime());
        assertEquals("https://pay.example/launch/token", result.getPaymentRedirectUrl());
        assertEquals("safe-token", result.getPaymentParameters().get("token"));
        ArgumentCaptor<PaymentOrderEntity> orderCaptor = ArgumentCaptor.forClass(PaymentOrderEntity.class);
        verify(orderMapper).insert(orderCaptor.capture());
        assertEquals(LocalDateTime.of(2026, 9, 6, 12, 30), orderCaptor.getValue().getExpireTime());
        verify(initiator).initiate(any(PaymentOrderEntity.class));
    }

    @Test
    void shouldRejectMissingOffShelfArchivedOrNotYetEffectivePlan() {
        StoragePlanMapper planMapper = mock(StoragePlanMapper.class);
        PaymentOrderMapper orderMapper = mock(PaymentOrderMapper.class);
        UserContextHolder.set(new AuthenticatedUser(7L, "UTC", "token"));
        PaymentOrderServiceImpl service = service(planMapper, orderMapper, mock(PaymentInitiator.class),
                mock(BusinessNumberGenerator.class));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createOrder(
                new CreatePaymentOrderDto().setPlanCode("INVALID").setPlanVersion(1).setClientRequestId("req-invalid")));

        assertSame(PAYMENT_PLAN_INVALID, exception.getErrorCode());
        verify(orderMapper, never()).insert(any(PaymentOrderEntity.class));
    }

    @Test
    void shouldReturnSameOrderForRepeatedClientRequestIdWithoutCreatingAgain() {
        PaymentOrderMapper orderMapper = mock(PaymentOrderMapper.class);
        PaymentOrderEntity existing = order().setClientRequestId("req-001");
        when(orderMapper.selectByUserAndClientRequestId(7L, "req-001")).thenReturn(existing);
        PaymentInitiator initiator = mock(PaymentInitiator.class);
        when(initiator.initiate(existing)).thenReturn(new PaymentInitiation("https://pay/retry", Map.of()));
        UserContextHolder.set(new AuthenticatedUser(7L, "UTC", "token"));
        PaymentOrderServiceImpl service = service(mock(StoragePlanMapper.class), orderMapper, initiator,
                mock(BusinessNumberGenerator.class));

        PaymentOrderVo result = service.createOrder(new CreatePaymentOrderDto()
                .setPlanCode("CHANGED").setPlanVersion(99).setClientRequestId("req-001"));

        assertEquals(existing.getOrderNo(), result.getOrderNo());
        assertEquals("基础套餐", result.getPlanNameSnapshot());
        verify(orderMapper, never()).insert(any(PaymentOrderEntity.class));
        verify(initiator).initiate(existing);
    }

    @Test
    void shouldReturnConcurrentExistingOrderWhenUniqueKeyConflicts() {
        StoragePlanMapper planMapper = mock(StoragePlanMapper.class);
        PaymentOrderMapper orderMapper = mock(PaymentOrderMapper.class);
        when(planMapper.selectActivePlan("BASIC", 3, utcNow())).thenReturn(plan());
        when(orderMapper.insert(any(PaymentOrderEntity.class))).thenThrow(new DuplicateKeyException("uk_order_user_request"));
        PaymentOrderEntity existing = order().setClientRequestId("req-race");
        when(orderMapper.selectByUserAndClientRequestId(7L, "req-race")).thenReturn(null, existing);
        PaymentInitiator initiator = mock(PaymentInitiator.class);
        when(initiator.initiate(existing)).thenReturn(new PaymentInitiation("https://pay/race", Map.of()));
        UserContextHolder.set(new AuthenticatedUser(7L, "UTC", "token"));

        PaymentOrderVo result = service(planMapper, orderMapper, initiator,
                mock(BusinessNumberGenerator.class)).createOrder(new CreatePaymentOrderDto()
                .setPlanCode("BASIC").setPlanVersion(3).setClientRequestId("req-race"));

        assertEquals(existing.getOrderNo(), result.getOrderNo());
        verify(initiator).initiate(existing);
    }

    @Test
    void shouldKeepOrderSnapshotAfterPlanChanges() {
        PaymentOrderMapper orderMapper = mock(PaymentOrderMapper.class);
        PaymentOrderEntity existing = order();
        when(orderMapper.selectByUserAndOrderNo(7L, existing.getOrderNo())).thenReturn(existing);
        UserContextHolder.set(new AuthenticatedUser(7L, "UTC", "token"));

        PaymentOrderVo result = service(mock(StoragePlanMapper.class), orderMapper, mock(PaymentInitiator.class),
                mock(BusinessNumberGenerator.class)).getOrder(existing.getOrderNo());

        assertEquals("基础套餐", result.getPlanNameSnapshot());
        assertEquals(9900L, result.getAmountCent());
    }

    @Test
    void shouldNotFindAnotherUsersOrder() {
        PaymentOrderMapper orderMapper = mock(PaymentOrderMapper.class);
        UserContextHolder.set(new AuthenticatedUser(8L, "UTC", "token"));

        BusinessException exception = assertThrows(BusinessException.class, () -> service(
                mock(StoragePlanMapper.class), orderMapper, mock(PaymentInitiator.class),
                mock(BusinessNumberGenerator.class)).getOrder("PO-OTHER"));

        assertSame(PAYMENT_ORDER_NOT_FOUND, exception.getErrorCode());
        verify(orderMapper).selectByUserAndOrderNo(8L, "PO-OTHER");
    }

    private static PaymentOrderServiceImpl service(StoragePlanMapper planMapper, PaymentOrderMapper orderMapper,
                                                   PaymentInitiator initiator, BusinessNumberGenerator generator) {
        return new PaymentOrderServiceImpl(planMapper, orderMapper, initiator, generator,
                Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofMinutes(30));
    }

    private static LocalDateTime utcNow() {
        return LocalDateTime.ofInstant(NOW, ZoneOffset.UTC);
    }

    private static StoragePlanEntity plan() {
        return new StoragePlanEntity().setId(11L).setPlanCode("BASIC").setPlanVersion(3)
                .setPlanName("基础套餐").setCapacityBytes(107374182400L).setDurationValue(12)
                .setDurationUnit(DurationUnitEnum.MONTH).setPriceCent(9900L).setCurrency(CurrencyEnum.CNY)
                .setStatus(StoragePlanStatusEnum.ACTIVE).setEffectiveTime(LocalDateTime.of(2026, 9, 1, 0, 0));
    }

    private static PaymentOrderEntity order() {
        return new PaymentOrderEntity().setId(21L).setOrderNo("PO20260906120000000EXISTING0001").setUserId(7L)
                .setStoragePlanId(11L).setPlanCodeSnapshot("BASIC").setPlanVersionSnapshot(3)
                .setPlanNameSnapshot("基础套餐").setCapacityBytesSnapshot(107374182400L)
                .setDurationValueSnapshot(12).setDurationUnitSnapshot(DurationUnitEnum.MONTH)
                .setAmountCent(9900L).setCurrency(CurrencyEnum.CNY).setPaymentChannel(PaymentChannelEnum.PINGPONG)
                .setStatus(PaymentOrderStatusEnum.PENDING).setExpireTime(LocalDateTime.of(2026, 9, 6, 12, 30));
    }
}
