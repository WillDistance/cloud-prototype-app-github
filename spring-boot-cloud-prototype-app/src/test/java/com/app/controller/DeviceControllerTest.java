package com.app.controller;

import com.app.pojo.dto.DeviceDto;
import com.app.pojo.vo.DeviceVo;
import com.app.security.AuthenticatedUser;
import com.app.security.UserContextHolder;
import com.app.service.DeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 设备控制器测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class DeviceControllerTest {

    private MockMvc mockMvc;
    private DeviceService deviceService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        deviceService = mock(DeviceService.class);
        DeviceController controller = new DeviceController();
        ReflectionTestUtils.setField(controller, "deviceService", deviceService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        UserContextHolder.set(new AuthenticatedUser(7L, "Asia/Shanghai", "token-id"));
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void 应查询当前用户绑定设备() throws Exception {
        when(deviceService.getMyDevice(7L)).thenReturn(new DeviceVo.BindResult()
                .setBindingId(11L).setDeviceId("CC-2026-AB12-8A2F").setModel("CloudCam C1")
                .setStatus("BOUND").setBindTime(LocalDateTime.of(2026, 9, 6, 1, 0)));

        mockMvc.perform(get("/api/device/getMyDevice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data.deviceId").value("CC-2026-AB12-8A2F"));

        verify(deviceService).getMyDevice(7L);
    }

    @Test
    void 应永久绑定当前用户设备() throws Exception {
        DeviceDto.BindDevice dto = new DeviceDto.BindDevice()
                .setDeviceId("CC-2026-AB12-8A2F").setPassword("CAMERA123");
        DeviceVo.BindResult result = new DeviceVo.BindResult();
        result.setGiftCapacityBytes(1073741824L).setBindingId(11L)
                .setDeviceId("CC-2026-AB12-8A2F").setModel("CloudCam C1").setStatus("BOUND");
        when(deviceService.bindDevice(7L, dto)).thenReturn(result);

        mockMvc.perform(post("/api/device/bindDevice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data.status").value("BOUND"));

        verify(deviceService).bindDevice(7L, dto);
    }
}
