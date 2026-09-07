package com.app.service;

import com.app.enums.ErrorCodeEnum;
import com.app.enums.UserLanguageEnum;
import com.app.enums.UserStatusEnum;
import com.app.exception.BusinessException;
import com.app.mapper.UserMapper;
import com.app.pojo.dto.UserDto;
import com.app.pojo.entity.UserEntity;
import com.app.pojo.vo.UserProfileVo;
import com.app.security.AuthenticatedUser;
import com.app.security.UserContextHolder;
import com.app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * 用户设置服务测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class UserServiceImplTest {
    private final UserMapper userMapper = mock(UserMapper.class);
    private final UserServiceImpl userService = new UserServiceImpl(userMapper);

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void shouldReadCurrentUserProfileOnlyFromJwtContext() {
        UserContextHolder.set(new AuthenticatedUser(7L, "Asia/Shanghai", "token-id"));
        when(userMapper.selectById(7L)).thenReturn(activeUser());

        UserProfileVo profile = userService.getProfile();

        assertEquals("user@example.com", profile.getEmail());
        assertEquals("Asia/Shanghai", profile.getTimeZone());
        assertEquals("zh-CN", profile.getPreferredLanguage());
        verify(userMapper).selectById(7L);
    }

    @Test
    void shouldPersistSupportedLanguageForCurrentUser() {
        UserContextHolder.set(new AuthenticatedUser(7L, "Asia/Shanghai", "token-id"));
        UserEntity user = activeUser();
        when(userMapper.selectById(7L)).thenReturn(user);
        when(userMapper.updateById(user)).thenReturn(1);

        UserProfileVo profile = userService.updateLanguage(new UserDto.UpdateLanguage().setLanguage("de"));

        assertEquals(UserLanguageEnum.DE, user.getPreferredLanguage());
        assertEquals("de", profile.getPreferredLanguage());
        verify(userMapper).updateById(user);
    }

    @Test
    void shouldRejectUnsupportedLanguageWithStableErrorCode() {
        UserContextHolder.set(new AuthenticatedUser(7L, "Asia/Shanghai", "token-id"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.updateLanguage(new UserDto.UpdateLanguage().setLanguage("fr")));

        assertEquals(ErrorCodeEnum.USER_LANGUAGE_UNSUPPORTED, exception.getErrorCode());
    }

    @Test
    void shouldRejectMissingJwtContext() {
        BusinessException exception = assertThrows(BusinessException.class, userService::getProfile);
        assertEquals(ErrorCodeEnum.AUTH_REQUIRED, exception.getErrorCode());
    }

    private UserEntity activeUser() {
        return new UserEntity().setId(7L).setEmail("user@example.com").setTimeZone("Asia/Shanghai")
                .setPreferredLanguage(UserLanguageEnum.ZH_CN).setStatus(UserStatusEnum.ACTIVE);
    }
}
