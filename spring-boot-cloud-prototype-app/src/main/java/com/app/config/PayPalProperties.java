package com.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * PayPal Checkout配置。密钥必须通过环境变量或密钥管理服务注入。
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment.paypal")
public class PayPalProperties {
    /** 是否启用PayPal支付。 */
    private boolean enabled;

    /** PayPal API地址，沙盒为https://api-m.sandbox.paypal.com。 */
    private String baseUrl = "https://api-m.sandbox.paypal.com";

    /** PayPal Client ID，可公开给前端SDK，但后端仍需独立保存。 */
    private String clientId;

    /** PayPal Client Secret，只能放在服务端密钥配置中。 */
    private String clientSecret;

    /** PayPal Webhook ID。 */
    private String webhookId;

    /** 结账页展示的商户名称。 */
    private String brandName = "Cloud Album";

    /** PayPal批准后回跳的H5地址。 */
    private String returnUrl;

    /** 用户取消支付后的H5地址。 */
    private String cancelUrl;

    /** 订单有效期，单位分钟。 */
    private int orderExpireMinutes = 30;

    /** PayPal收单允许的币种。CNY不在跨境PayPal订单的默认支持范围内。 */
    private Set<String> supportedCurrencies = new LinkedHashSet<>(Set.of("USD", "EUR", "GBP"));
}
