package com.app.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户注册结果
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
public class AuthRegisterVo {
    private Long userId;
    private String email;
    private String timeZone;
    private String preferredLanguage;
}
