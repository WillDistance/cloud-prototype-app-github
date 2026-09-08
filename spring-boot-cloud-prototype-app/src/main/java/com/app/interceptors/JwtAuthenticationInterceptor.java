package com.app.interceptors;

import com.app.enums.ErrorCodeEnum;
import com.app.exception.AuthenticationException;
import com.app.security.JwtTokenService;
import com.app.utils.UserContextHolderUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT请求鉴权拦截器
 *
 * @author yanlei
 * @since 2026-09-06
 */
public class JwtAuthenticationInterceptor implements HandlerInterceptor {
    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationInterceptor(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() <= 7) {
            throw new AuthenticationException(ErrorCodeEnum.AUTH_REQUIRED);
        }
        UserContextHolderUtil.set(jwtTokenService.parse(authorization.substring(7)));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        UserContextHolderUtil.clear();
    }
}
