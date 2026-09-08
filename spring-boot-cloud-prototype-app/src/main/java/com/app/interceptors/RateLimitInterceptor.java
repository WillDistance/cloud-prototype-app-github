package com.app.interceptors;

import com.app.enums.ErrorCodeEnum;
import com.app.exception.BusinessException;
import com.app.support.ratelimit.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * 入口限流拦截器，不记录或读取敏感凭证。
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter;

    public RateLimitInterceptor(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        String category = category(uri);
        String ip = request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
        int limit = "auth".equals(category) ? 30 : "callback".equals(category) ? 120 : 300;
        if (!rateLimiter.tryAcquire(category + ":ip:" + ip, limit, Duration.ofMinutes(1))) {
            throw new BusinessException(ErrorCodeEnum.SYSTEM_BUSY);
        }
        return true;
    }

    /**
     * 处理category相关的业务逻辑。
     *
     * @param uri 方法参数（uri）
     * @return 处理结果
     */
    private String category(String uri) {
        if (uri != null && uri.contains("/auth/")) {
            return "auth";
        }
        if (uri != null && uri.contains("/ossCallback/")) {
            return "callback";
        }
        if (uri != null && uri.contains("/deviceUpload/")) {
            return "upload";
        }
        return "api";
    }
}
