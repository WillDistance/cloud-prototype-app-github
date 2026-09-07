package com.app.exception;

import com.app.enums.ErrorCodeEnum;
import com.app.pojo.CommonResult;
import com.app.test.TestExceptionController;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 全局异常处理器测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class GlobalExceptionHandlerTest {

    @Test
    void shouldMapBusinessExceptionToCommonResult() throws Exception {
        MockMvc mockMvc = createMockMvc();

        mockMvc.perform(get("/test-exception/business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("A10001"))
                .andExpect(jsonPath("$.errorMag").value("邮箱或密码错误"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldMapAuthenticationExceptionToCommonResult() throws Exception {
        MockMvc mockMvc = createMockMvc();

        mockMvc.perform(get("/test-exception/authentication"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("A10004"));
    }

    @Test
    void shouldMapRequestParameterExceptionToCommonResult() throws Exception {
        MockMvc mockMvc = createMockMvc();

        mockMvc.perform(get("/test-exception/parameter"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("S40000"))
                .andExpect(jsonPath("$.errorMag").value("测试参数错误"));
    }

    @Test
    void shouldMapValidationExceptionWithoutExposingFrameworkDetails() throws Exception {
        MockMvc mockMvc = createMockMvc();

        mockMvc.perform(post("/test-exception/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("S40000"));
    }

    @Test
    void shouldMapUnknownExceptionToStableSystemError() throws Exception {
        MockMvc mockMvc = createMockMvc();

        mockMvc.perform(get("/test-exception/system"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("S50000"))
                .andExpect(jsonPath("$.errorMag").value("系统繁忙，请稍后重试"));
    }

    @Test
    void shouldCreateFailedResultFromException() {
        CommonResult<Void> result = CommonResult.fail(new BusinessException(ErrorCodeEnum.PHOTO_NOT_FOUND));

        assertEquals(ErrorCodeEnum.PHOTO_NOT_FOUND, result.getErrorCode());
        assertEquals("照片不存在", result.getErrorMag());
        assertNull(result.getData());
    }

    private MockMvc createMockMvc() {
        return MockMvcBuilders.standaloneSetup(new TestExceptionController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
}
