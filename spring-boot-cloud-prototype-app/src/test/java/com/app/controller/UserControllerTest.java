package com.app.controller;

import com.app.enums.ErrorCodeEnum;
import com.app.exception.BusinessException;
import com.app.exception.GlobalExceptionHandler;
import com.app.pojo.dto.UserDto;
import com.app.pojo.vo.UserProfileVo;
import com.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户设置控制器测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class UserControllerTest {
    @Test
    void shouldGetCurrentUserProfile() throws Exception {
        UserService service = mock(UserService.class);
        when(service.getProfile()).thenReturn(profile("zh-CN"));

        mockMvc(service).perform(get("/api/user/getProfile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.timeZone").value("Asia/Shanghai"))
                .andExpect(jsonPath("$.data.preferredLanguage").value("zh-CN"));
        verify(service).getProfile();
    }

    @Test
    void shouldUpdateLanguageUsingPostBody() throws Exception {
        UserService service = mock(UserService.class);
        when(service.updateLanguage(any(UserDto.UpdateLanguage.class))).thenReturn(profile("de"));

        mockMvc(service).perform(post("/api/user/updateLanguage")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"language\":\"de\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data.preferredLanguage").value("de"));
        verify(service).updateLanguage(any(UserDto.UpdateLanguage.class));
    }

    @Test
    void shouldReturnStableCodeForUnsupportedLanguage() throws Exception {
        UserService service = mock(UserService.class);
        when(service.updateLanguage(any(UserDto.UpdateLanguage.class)))
                .thenThrow(new BusinessException(ErrorCodeEnum.USER_LANGUAGE_UNSUPPORTED));

        mockMvc(service).perform(post("/api/user/updateLanguage")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"language\":\"fr\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("U20001"));
    }

    private MockMvc mockMvc(UserService service) {
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", service);
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private UserProfileVo profile(String language) {
        return new UserProfileVo().setEmail("user@example.com").setTimeZone("Asia/Shanghai")
                .setPreferredLanguage(language);
    }
}
