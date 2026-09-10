package com.app.config;

import com.app.interceptors.JwtAuthenticationInterceptor;
import com.app.interceptors.RateLimitInterceptor;
import com.app.security.JwtTokenService;
import com.app.support.ratelimit.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * JWT安全配置
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Configuration
public class JwtSecurityConfig {
    @Bean
    public JwtTokenService jwtTokenService(@Value("${security.jwt.secret}") String secret,
                                           @Value("${security.jwt.ttl}") Duration ttl) {
        return new JwtTokenService(secret, ttl);
    }

    @Bean
    public WebMvcConfigurer jwtWebMvcConfigurer(JwtTokenService jwtTokenService, RateLimiter rateLimiter) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new RateLimitInterceptor(rateLimiter)).addPathPatterns("/api/**");
                registry.addInterceptor(new JwtAuthenticationInterceptor(jwtTokenService)).addPathPatterns("/api/**")
                        .excludePathPatterns("/api/auth/login", "/api/auth/register", "/api/auth/sendRegisterCode", "/api/auth/verifyRegisterCode",
                                "/api/auth/sendResetPasswordCode", "/api/auth/verifyResetPasswordCode", "/api/auth/resetPassword", "/api/auth/refresh",
                                "/api/deviceUpload/createUploadSession", "/api/ossCallback/uploadCompleted",
                                "/api/paymentOrder/webhook/paypal", "/error");
            }
        };
    }
}
