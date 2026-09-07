package com.app.controller;

import com.app.pojo.dto.DeviceUploadDto;
import com.app.pojo.vo.DeviceUploadVo;
import com.app.service.DeviceUploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 设备上传控制器测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class DeviceUploadControllerTest {

    private MockMvc mockMvc;
    private DeviceUploadService deviceUploadService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        deviceUploadService = mock(DeviceUploadService.class);
        DeviceUploadController controller = new DeviceUploadController();
        ReflectionTestUtils.setField(controller, "deviceUploadService", deviceUploadService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void 应通过设备鉴权请求头创建上传会话() throws Exception {
        DeviceUploadDto.CreateSession body = new DeviceUploadDto.CreateSession()
                .setOriginalFileName("photo.jpg").setContentType("image/jpeg").setFileSizeBytes(1024L);
        when(deviceUploadService.createUploadSession(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new DeviceUploadVo.CreateSession().setUploadNo("UPL001").setMethod("PUT"));

        mockMvc.perform(post("/api/deviceUpload/createUploadSession")
                        .header("X-Device-Id", "CC-2026-AB12-8A2F")
                        .header("X-Device-Password", "CAMERA123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data.uploadNo").value("UPL001"));

        ArgumentCaptor<DeviceUploadDto.CreateSession> captor = ArgumentCaptor.forClass(DeviceUploadDto.CreateSession.class);
        verify(deviceUploadService).createUploadSession(captor.capture());
        assertEquals("CC-2026-AB12-8A2F", captor.getValue().getDeviceId());
        assertEquals("CAMERA123", captor.getValue().getPassword());
    }

    @Test
    void 应通过设备鉴权请求头查询上传状态() throws Exception {
        when(deviceUploadService.getUploadSessionStatus(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new DeviceUploadVo.SessionStatus().setUploadNo("UPL001").setStatus("URL_ISSUED")
                        .setExpireTime(LocalDateTime.of(2026, 9, 6, 2, 0)));

        mockMvc.perform(get("/api/deviceUpload/getUploadSessionStatus")
                        .header("X-Device-Id", "CC-2026-AB12-8A2F")
                        .header("X-Device-Password", "CAMERA123")
                        .param("uploadNo", "UPL001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("URL_ISSUED"));
    }
}
