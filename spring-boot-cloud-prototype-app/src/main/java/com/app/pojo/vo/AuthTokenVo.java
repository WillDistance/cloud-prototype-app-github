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
    /** Access Token。 */
    private String accessToken;

    /** Refresh Token。 */
    private String refreshToken;

    /** 令牌类型。 */
    private String tokenType;

    /** Access Token有效期，单位为秒。 */
    private long accessTokenExpiresIn;

    /** Refresh Token有效期，单位为秒。 */
    private long refreshTokenExpiresIn;
}
