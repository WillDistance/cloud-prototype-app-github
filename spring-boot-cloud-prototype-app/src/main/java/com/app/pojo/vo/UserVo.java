package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户资料响应。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Getter
@AllArgsConstructor
public class UserVo {
    /** 用户主键ID。 */
    private Long id;

    /** 用户登录邮箱。 */
    private String email;

    /** 用户IANA时区。 */
    private String timeZone;

    /** 用户偏好语言。 */
    private String preferredLanguage;
}
