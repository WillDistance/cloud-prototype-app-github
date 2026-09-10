package com.app.support.payment.paypal;

import com.app.config.PayPalProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * PayPal REST API适配器。
 *
 * <p>银行卡号和PayPal凭证均不经过本应用。前端使用PayPal托管收银台，
 * 本适配器只负责服务端创建订单、执行扣款和验证Webhook。</p>
 */
@Component
public class PayPalGateway {
    private static final String COMPLETED_EVENT = "PAYMENT.CAPTURE.COMPLETED";

    private final ObjectMapper objectMapper;
    private final PayPalProperties properties;
    private final HttpClient httpClient;
    private volatile CachedToken cachedToken;

    public PayPalGateway(ObjectMapper objectMapper, PayPalProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public PayPalOrderResult createOrder(String orderNo, String planName, long amountCent, String currency,
                                         String returnUrl, String cancelUrl) {
        requireConfigured();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("intent", "CAPTURE");
        ArrayNode purchaseUnits = payload.putArray("purchase_units");
        ObjectNode purchaseUnit = purchaseUnits.addObject();
        purchaseUnit.put("reference_id", orderNo);
        purchaseUnit.put("invoice_id", orderNo);
        purchaseUnit.put("custom_id", orderNo);
        purchaseUnit.put("description", planName);
        ObjectNode amount = purchaseUnit.putObject("amount");
        amount.put("currency_code", currency);
        amount.put("value", formatAmount(amountCent));

        ObjectNode context = payload.putObject("application_context");
        context.put("brand_name", properties.getBrandName());
        context.put("user_action", "PAY_NOW");
        context.put("shipping_preference", "NO_SHIPPING");
        context.put("return_url", returnUrl);
        context.put("cancel_url", cancelUrl);

        JsonNode response = request("POST", "/v2/checkout/orders", payload.toString(),
                Map.of("PayPal-Request-Id", orderNo));
        String providerOrderId = text(response, "id");
        String checkoutUrl = null;
        for (JsonNode link : response.path("links")) {
            if ("approve".equalsIgnoreCase(text(link, "rel"))) {
                checkoutUrl = text(link, "href");
                break;
            }
        }
        if (providerOrderId == null || checkoutUrl == null) {
            throw new PayPalGatewayException("PayPal create order response is missing id or approve link");
        }
        return new PayPalOrderResult(providerOrderId, checkoutUrl);
    }

    public PayPalCaptureResult captureOrder(String providerOrderId, String requestId) {
        requireConfigured();
        JsonNode response = request("POST", "/v2/checkout/orders/" + url(providerOrderId) + "/capture", "{}",
                Map.of("PayPal-Request-Id", requestId));
        String captureId = response.path("purchase_units").path(0).path("payments").path("captures")
                .path(0).path("id").asText(null);
        String status = response.path("status").asText(null);
        return new PayPalCaptureResult(captureId, status);
    }

    /**
     * 使用PayPal官方验证接口验证Webhook签名，再解析事件内容。
     */
    public PayPalWebhookEvent verifyWebhook(String rawPayload, String transmissionId, String transmissionTime,
                                            String certUrl, String authAlgo, String transmissionSignature) {
        JsonNode event = parse(rawPayload);
        String eventId = text(event, "id");
        String eventType = text(event, "event_type");
        if (!configuredForWebhook(transmissionId, transmissionTime, certUrl, authAlgo, transmissionSignature)
                || properties.getWebhookId() == null || properties.getWebhookId().isBlank()) {
            return parseWebhookEvent(event, rawPayload, eventId, eventType, transmissionSignature, false);
        }

        ObjectNode verification = objectMapper.createObjectNode();
        verification.put("auth_algo", authAlgo);
        verification.put("cert_url", certUrl);
        verification.put("transmission_id", transmissionId);
        verification.put("transmission_sig", transmissionSignature);
        verification.put("transmission_time", transmissionTime);
        verification.put("webhook_id", properties.getWebhookId());
        verification.set("webhook_event", event);
        JsonNode response = request("POST", "/v1/notifications/verify-webhook-signature",
                verification.toString(), Map.of());
        boolean verified = "SUCCESS".equalsIgnoreCase(response.path("verification_status").asText());
        return parseWebhookEvent(event, rawPayload, eventId, eventType, transmissionSignature, verified);
    }

    public boolean isPaymentCompleted(PayPalWebhookEvent event) {
        return COMPLETED_EVENT.equals(event.eventType())
                && "COMPLETED".equalsIgnoreCase(event.providerStatus());
    }

    private PayPalWebhookEvent parseWebhookEvent(JsonNode event, String rawPayload, String eventId,
                                                 String eventType, String signatureValue, boolean verified) {
        JsonNode resource = event.path("resource");
        String providerOrderId = resource.path("supplementary_data").path("related_ids")
                .path("order_id").asText(null);
        String orderNo = firstNonBlank(
                resource.path("invoice_id").asText(null),
                resource.path("custom_id").asText(null),
                resource.path("reference_id").asText(null));
        String providerCaptureId = resource.path("id").asText(null);
        String currency = resource.path("amount").path("currency_code").asText(null);
        Long amountCent = toCent(resource.path("amount").path("value").asText(null));
        String status = resource.path("status").asText(null);
        return new PayPalWebhookEvent(eventId, eventType, orderNo, providerOrderId, providerCaptureId,
                amountCent, currency, status, rawPayload, signatureValue, verified);
    }

    private JsonNode request(String method, String path, String body, Map<String, String> extraHeaders) {
        try {
            String token = accessToken();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl(path)))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json");
            extraHeaders.forEach(builder::header);
            HttpRequest request = builder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode parsed = parse(response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new PayPalGatewayException("PayPal API request failed with status " + response.statusCode()
                        + " and error " + parsed.path("name").asText("unknown"));
            }
            return parsed;
        } catch (PayPalGatewayException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new PayPalGatewayException("PayPal API request failed", exception);
        }
    }

    private String accessToken() {
        CachedToken current = cachedToken;
        if (current != null && Instant.now().isBefore(current.expiresAt())) {
            return current.value();
        }
        synchronized (this) {
            current = cachedToken;
            if (current != null && Instant.now().isBefore(current.expiresAt())) {
                return current.value();
            }
            try {
                String credentials = Base64.getEncoder().encodeToString(
                        (properties.getClientId() + ":" + properties.getClientSecret()).getBytes(StandardCharsets.UTF_8));
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl("/v1/oauth2/token")))
                        .timeout(Duration.ofSeconds(20))
                        .header("Authorization", "Basic " + credentials)
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials", StandardCharsets.UTF_8))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                JsonNode body = parse(response.body());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new PayPalGatewayException("PayPal OAuth request failed with status " + response.statusCode());
                }
                String token = body.path("access_token").asText(null);
                long expiresIn = body.path("expires_in").asLong(300);
                if (token == null || token.isBlank()) {
                    throw new PayPalGatewayException("PayPal OAuth response is missing access token");
                }
                cachedToken = new CachedToken(token, Instant.now().plusSeconds(Math.max(30, expiresIn - 60)));
                return token;
            } catch (PayPalGatewayException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new PayPalGatewayException("PayPal OAuth request failed", exception);
            }
        }
    }

    private void requireConfigured() {
        if (!properties.isEnabled() || isBlank(properties.getClientId()) || isBlank(properties.getClientSecret())) {
            throw new PayPalGatewayException("PayPal payment is not configured");
        }
    }

    private boolean configuredForWebhook(String transmissionId, String transmissionTime, String certUrl,
                                         String authAlgo, String transmissionSignature) {
        return !isBlank(transmissionId) && !isBlank(transmissionTime) && !isBlank(certUrl)
                && !isBlank(authAlgo) && !isBlank(transmissionSignature);
    }

    private String apiUrl(String path) {
        String base = properties.getBaseUrl();
        if (base == null || base.isBlank()) {
            throw new PayPalGatewayException("PayPal base URL is not configured");
        }
        return base.replaceAll("/+$", "") + path;
    }

    private JsonNode parse(String body) {
        try {
            return objectMapper.readTree(body == null || body.isBlank() ? "{}" : body);
        } catch (Exception exception) {
            throw new PayPalGatewayException("PayPal response is not valid JSON", exception);
        }
    }

    private static String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? null : value;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static Long toCent(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value).movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException exception) {
            return null;
        }
    }

    private static String formatAmount(long amountCent) {
        if (amountCent <= 0) {
            throw new PayPalGatewayException("Payment amount must be positive");
        }
        return BigDecimal.valueOf(amountCent, 2).setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }

    private static String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record PayPalOrderResult(String providerOrderId, String checkoutUrl) {
    }

    public record PayPalCaptureResult(String providerCaptureId, String providerStatus) {
    }

    public record PayPalWebhookEvent(String eventId, String eventType, String orderNo, String providerOrderId,
                                     String providerCaptureId, Long amountCent, String currency,
                                     String providerStatus, String rawPayload, String signatureValue,
                                     boolean signatureVerified) {
    }

    public static class PayPalGatewayException extends RuntimeException {
        public PayPalGatewayException(String message) {
            super(message);
        }

        public PayPalGatewayException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private record CachedToken(String value, Instant expiresAt) {
    }
}
