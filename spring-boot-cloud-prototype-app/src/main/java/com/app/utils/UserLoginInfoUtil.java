package com.app.utils;

import com.app.enums.ErrorCodeEnum;
import com.app.exception.AuthenticationException;
import com.app.security.AuthenticatedUser;
import com.app.security.UserContextHolder;
import org.springframework.stereotype.Component;

/**
 * 用户登录信息工具类
 *
 * @author yanlei
 * @since 2026-09-05
 */
@Component
public class UserLoginInfoUtil {
    public String getUserId() {
        AuthenticatedUser user = UserContextHolder.get();
        if (user == null) throw new AuthenticationException(ErrorCodeEnum.AUTH_REQUIRED);
        return String.valueOf(user.userId());
    }
}
