package com.app.mapper;

import com.app.pojo.entity.PaymentOrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * t_payment_order表数据访问接口
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrderEntity> {
    /**
     * 按主键查询并锁定数据库记录。
     *
     * @param id 支付订单主键ID
     * @return 查询结果
     */
    /**
     * 执行数据库查询操作。
     *
     * @param id 数据库查询参数
     * @return 数据库查询结果
     */
    PaymentOrderEntity selectByIdForUpdate(Long id);

    PaymentOrderEntity selectByUserAndClientRequestId(@Param("userId") Long userId, @Param("clientRequestId") String clientRequestId);

    PaymentOrderEntity selectByUserAndOrderNo(@Param("userId") Long userId, @Param("orderNo") String orderNo);

    PaymentOrderEntity selectByOrderNo(@Param("orderNo") String orderNo);

    PaymentOrderEntity selectByProviderOrderId(@Param("providerOrderId") String providerOrderId);
    /**
     * 在期望状态匹配时原子更新记录状态。
     *
     * @param id 数据库记录主键ID
     * @param status 目标状态
     * @param expectedStatus 期望的当前状态
     * @return 受影响的记录数
     */
    /**
     * 执行数据库更新操作。
     *
     * @param id             数据库查询参数
     * @param status         数据库查询参数
     * @param expectedStatus 数据库查询参数
     * @return 受影响的记录数
     */
    int updateStatusById(@Param("id") Long id, @Param("status") String status, @Param("expectedStatus") String expectedStatus);

    int updateProviderOrder(@Param("id") Long id, @Param("providerOrderId") String providerOrderId,
                            @Param("checkoutUrl") String checkoutUrl);

    int updateProviderCapture(@Param("id") Long id, @Param("providerCaptureId") String providerCaptureId);

    int markPaid(@Param("id") Long id, @Param("providerCaptureId") String providerCaptureId,
                 @Param("paidTime") java.time.LocalDateTime paidTime);
}
