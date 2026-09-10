package com.app.mapper;

import com.app.pojo.entity.PaymentCallbackEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * t_payment_callback表数据访问接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Mapper
public interface PaymentCallbackMapper extends BaseMapper<PaymentCallbackEntity> {
    /**
     * 按主键查询并锁定数据库记录。
     *
     * @param id 数据库查询参数
     * @return 查询结果
     */
    /**
     * 执行数据库查询操作。
     *
     * @param id 数据库查询参数
     * @return 数据库查询结果
     */
    PaymentCallbackEntity selectByIdForUpdate(Long id);

    PaymentCallbackEntity selectByChannelAndEventId(@Param("paymentChannel") String paymentChannel,
                                                    @Param("callbackEventId") String callbackEventId);
}
