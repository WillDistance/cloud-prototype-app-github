package com.app.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 用户注册响应。 */
@Getter
@AllArgsConstructor
public class UserVo {
    private Long id;
    private String email;
    private String timeZone;
    private String preferredLanguage;
}
