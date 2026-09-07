package com.app.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户登录结果
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
public class AuthLoginVo {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
}