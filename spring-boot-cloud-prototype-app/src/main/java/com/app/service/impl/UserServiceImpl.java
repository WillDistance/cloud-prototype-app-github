package com.app.service.impl;

import com.app.enums.ErrorCodeEnum;
import com.app.enums.UserLanguageEnum;
import com.app.enums.UserStatusEnum;
import com.app.exception.AuthenticationException;
import com.app.exception.BusinessException;
import com.app.mapper.UserMapper;
import com.app.pojo.dto.UserDto;
import com.app.pojo.entity.UserEntity;
import com.app.pojo.vo.UserProfileVo;
import com.app.security.AuthenticatedUser;
import com.app.security.UserContextHolder;
import com.app.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 用户设置服务实现
 *
 * @author yanlei
 * @since 2026-09-06
 */
@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserProfileVo getProfile() {
        return toProfile(findCurrentUser());
    }

    @Override
    public UserProfileVo updateLanguage(UserDto.UpdateLanguage dto) {
        UserLanguageEnum language = parseLanguage(dto.getLanguage());
        UserEntity user = findCurrentUser();
        user.setPreferredLanguage(language);
        userMapper.updateById(user);
        return toProfile(user);
    }

    /**
     * 查询当前认证用户的用户实体。
     *
     * @return 方法处理后的结果
     */
    private UserEntity findCurrentUser() {
        AuthenticatedUser authenticatedUser = UserContextHolder.get();
        if (authenticatedUser == null) throw new AuthenticationException(ErrorCodeEnum.AUTH_REQUIRED);
        UserEntity user = userMapper.selectById(authenticatedUser.userId());
        if (user == null || user.getStatus() != UserStatusEnum.ACTIVE) {
            throw new AuthenticationException(ErrorCodeEnum.AUTH_REQUIRED);
        }
        return user;
    }

    /**
     * 解析请求语言并转换为系统语言枚举。
     *
     * @param language 语言标识
     * @return 方法处理后的结果
     */
    private UserLanguageEnum parseLanguage(String language) {
        return Arrays.stream(UserLanguageEnum.values()).filter(item -> item.getValue().equals(language))
                .findFirst().orElseThrow(() -> new BusinessException(ErrorCodeEnum.USER_LANGUAGE_UNSUPPORTED));
    }

    /**
     * 将用户实体转换为用户资料视图对象。
     *
     * @param user 用户实体
     * @return 方法处理后的结果
     */
    private UserProfileVo toProfile(UserEntity user) {
        return new UserProfileVo().setEmail(user.getEmail()).setTimeZone(user.getTimeZone())
                .setPreferredLanguage(user.getPreferredLanguage().getValue());
    }
}
