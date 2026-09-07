package com.app.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 当前用户资料响应
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Data
@Accessors(chain = true)
public class UserProfileVo {
    private String email;
    private String timeZone;
    private String preferredLanguage;
}
