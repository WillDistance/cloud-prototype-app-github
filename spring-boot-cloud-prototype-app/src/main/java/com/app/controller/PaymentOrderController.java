package com.app.controller;

import com.app.pojo.CommonResult;
import com.app.pojo.dto.CapturePaymentOrderRequest;
import com.app.pojo.dto.CreatePaymentOrderRequest;
import com.app.pojo.vo.PaymentOrderVo;
import com.app.exception.PaymentWebhookException;
import com.app.service.PaymentOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付订单控制器。
 */
@RestController
@RequestMapping("/api/paymentOrder")
public class PaymentOrderController {
    @Autowired
    private PaymentOrderService paymentOrderService;

    @PostMapping("/createOrder")
    public CommonResult<PaymentOrderVo> createOrder(@Valid @RequestBody CreatePaymentOrderRequest request) {
        return CommonResult.success(paymentOrderService.createOrder(request));
    }

    @PostMapping("/captureOrder")
    public CommonResult<PaymentOrderVo> captureOrder(@Valid @RequestBody CapturePaymentOrderRequest request) {
        return CommonResult.success(paymentOrderService.captureOrder(request));
    }

    @GetMapping("/getOrder")
    public CommonResult<PaymentOrderVo> getOrder(@RequestParam String orderNo) {
        return CommonResult.success(paymentOrderService.getOrder(orderNo));
    }

    /**
     * PayPal Webhook不使用用户JWT；签名由PayPal官方接口验证。
     */
    @PostMapping("/webhook/paypal")
    public ResponseEntity<Void> paypalWebhook(@RequestBody String rawPayload, HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("paypal-transmission-id", request.getHeader("PAYPAL-TRANSMISSION-ID"));
        headers.put("paypal-transmission-time", request.getHeader("PAYPAL-TRANSMISSION-TIME"));
        headers.put("paypal-cert-url", request.getHeader("PAYPAL-CERT-URL"));
        headers.put("paypal-auth-algo", request.getHeader("PAYPAL-AUTH-ALGO"));
        headers.put("paypal-transmission-sig", request.getHeader("PAYPAL-TRANSMISSION-SIG"));
        try {
            paymentOrderService.handlePayPalWebhook(rawPayload, headers);
            return ResponseEntity.ok().build();
        } catch (PaymentWebhookException exception) {
            return ResponseEntity.status(exception.isRejected() ? HttpStatus.BAD_REQUEST
                    : HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
