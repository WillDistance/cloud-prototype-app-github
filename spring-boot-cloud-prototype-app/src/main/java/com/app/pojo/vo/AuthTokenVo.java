package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 登录令牌响应。 */
@Getter
@AllArgsConstructor
public class AuthTokenVo {
    private String token;
    private String tokenType;
    private long expiresIn;
}
