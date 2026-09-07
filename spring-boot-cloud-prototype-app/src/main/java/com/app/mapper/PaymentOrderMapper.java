package com.app.mapper;

import com.app.pojo.entity.PaymentOrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * PaymentOrderEntity数据访问接口
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrderEntity> {
    /**
     * 按用户与客户端幂等号查询订单
     *
     * @param userId          用户ID
     * @param clientRequestId 客户端幂等号
     * @return 支付订单
     */
    PaymentOrderEntity selectByUserAndClientRequestId(@Param("userId") Long userId,
                                                      @Param("clientRequestId") String clientRequestId);

    /**
     * 按用户与订单号查询订单
     *
     * @param userId  用户ID
     * @param orderNo 订单号
     * @return 支付订单
     */
    PaymentOrderEntity selectByUserAndOrderNo(@Param("userId") Long userId, @Param("orderNo") String orderNo);
}
