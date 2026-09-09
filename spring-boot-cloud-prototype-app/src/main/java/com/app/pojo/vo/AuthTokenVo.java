package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 双令牌登录响应。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Getter
@AllArgsConstructor
public class AuthTokenVo {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long accessTokenExpiresIn;
    private long refreshTokenExpiresIn;
}
