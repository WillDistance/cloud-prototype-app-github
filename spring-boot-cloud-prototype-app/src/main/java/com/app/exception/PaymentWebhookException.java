package com.app.exception;

/**
 * 支付Webhook处理异常。
 */
public class PaymentWebhookException extends RuntimeException {
    private final boolean rejected;

    public PaymentWebhookException(String message, boolean rejected) {
        super(message);
        this.rejected = rejected;
    }

    public PaymentWebhookException(String message, Throwable cause, boolean rejected) {
        super(message, cause);
        this.rejected = rejected;
    }

    public boolean isRejected() {
        return rejected;
    }
}
