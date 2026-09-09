package com.app.service;

import com.app.pojo.dto.UpdateLanguageRequest;
import com.app.pojo.entity.UserEntity;
import com.app.pojo.vo.UserVo;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * 用户资料业务接口。
 *
 * @author yanlei
 * @since 2026-09-09
 */
public interface UserService extends IService<UserEntity> {
    /**
     * 查询当前登录用户的公开资料。
     *
     * @return 当前登录用户资料
     */
    UserVo getProfile();

    /**
     * 更新当前登录用户的语言偏好。
     *
     * @param request 用户语言偏好请求
     * @return 更新后的用户资料
     */
    UserVo updateLanguage(UpdateLanguageRequest request);
}
