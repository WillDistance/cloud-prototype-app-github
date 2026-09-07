package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.CreatePaymentOrderDto;
import com.app.pojo.vo.PaymentOrderVo;
import com.app.service.PaymentOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 支付订单控制器
 *
 * @author yanlei
 * @since 2026-09-06
 */
@RestController
@RequestMapping("/api/paymentOrder")
public class PaymentOrderController {
    @Autowired
    private PaymentOrderService paymentOrderService;

    /**
     * 创建支付订单
     *
     * @param dto 创建订单请求
     * @return 支付订单
     */
    @PostMapping("/createOrder")
    public CommonResult<PaymentOrderVo> createOrder(@Valid @RequestBody CreatePaymentOrderDto dto) {
        return CommonResult.success(paymentOrderService.createOrder(dto));
    }

    /**
     * 查询当前用户订单
     *
     * @param orderNo 订单号
     * @return 支付订单
     */
    @GetMapping("/getOrder")
    public CommonResult<PaymentOrderVo> getOrder(@RequestParam("orderNo") String orderNo) {
        return CommonResult.success(paymentOrderService.getOrder(orderNo));
    }
}
