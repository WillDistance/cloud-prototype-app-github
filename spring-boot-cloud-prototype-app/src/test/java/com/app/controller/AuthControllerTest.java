package com.app.controller;

import com.app.pojo.dto.AuthDto;
import com.app.pojo.vo.AuthLoginVo;
import com.app.pojo.vo.AuthRegisterVo;
import com.app.security.AuthenticatedUser;
import com.app.security.UserContextHolder;
import com.app.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证控制器测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class AuthControllerTest {
    private MockMvc mockMvc;
    private AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authService", authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldSendRegisterCodeAtExpectedPath() throws Exception {
        AuthDto.SendRegisterCode request = new AuthDto.SendRegisterCode().setEmail(" User@Example.COM ");
        when(authService.sendRegisterCode(request, "127.0.0.1")).thenReturn(true);
        mockMvc.perform(post("/api/auth/sendRegisterCode").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data").value(true));
        verify(authService).sendRegisterCode(request, "127.0.0.1");
    }

    @Test
    void shouldVerifyRegisterCodeAtExpectedPath() throws Exception {
        AuthDto.VerifyRegisterCode request = new AuthDto.VerifyRegisterCode().setEmail("user@example.com").setCode("123456");
        when(authService.verifyRegisterCode(request)).thenReturn(true);
        mockMvc.perform(post("/api/auth/verifyRegisterCode").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));
        verify(authService).verifyRegisterCode(request);
    }

    @Test
    void shouldRegisterWithRequiredHeaders() throws Exception {
        AuthDto.Register request = new AuthDto.Register().setEmail("user@example.com").setPassword("password123").setConfirmPassword("password123");
        when(authService.register(eq(request), eq("Asia/Shanghai"), eq("zh-CN")))
                .thenReturn(new AuthRegisterVo().setUserId(1L).setEmail("user@example.com").setTimeZone("Asia/Shanghai").setPreferredLanguage("zh-CN"));
        mockMvc.perform(post("/api/auth/register").header("X-Time-Zone", "Asia/Shanghai").header("Accept-Language", "zh-CN")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.preferredLanguage").value("zh-CN"));
        verify(authService).register(request, "Asia/Shanghai", "zh-CN");
    }

    @Test
    void shouldExposeStageThreeAuthenticationEndpointsWithoutUserIdInput() throws Exception {
        AuthDto.Login login = new AuthDto.Login().setEmail("user@example.com").setPassword("password123");
        when(authService.login(login)).thenReturn(new AuthLoginVo().setAccessToken("jwt").setTokenType("Bearer").setExpiresIn(7200L));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.accessToken").value("jwt"));

        AuthDto.ResetPassword reset = new AuthDto.ResetPassword().setEmail("user@example.com").setNewPassword("newPassword123").setConfirmPassword("newPassword123");
        when(authService.resetPassword(reset)).thenReturn(true);
        mockMvc.perform(post("/api/auth/resetPassword").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reset)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));

        UserContextHolder.set(new AuthenticatedUser(9L, "UTC", "token-id"));
        when(authService.logout("token-id")).thenReturn(true);
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));
        UserContextHolder.clear();
        verify(authService).logout("token-id");
    }
}
