package com.app.controller;

import com.app.pojo.dto.OssCallbackDto;
import com.app.pojo.vo.OssCallbackVo;
import com.app.service.OssCallbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OSS回调控制器测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class OssCallbackControllerTest {
    @Test
    void 应仅绑定请求并调用服务() throws Exception {
        OssCallbackService service = mock(OssCallbackService.class);
        when(service.uploadCompleted(org.mockito.ArgumentMatchers.any())).thenReturn(new OssCallbackVo()
                .setUploadNo("UPL001").setPhotoNo("PHT001").setStatus("COMPLETED").setIdempotent(false));
        OssCallbackController controller = new OssCallbackController();
        ReflectionTestUtils.setField(controller, "ossCallbackService", service);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        OssCallbackDto dto = new OssCallbackDto().setEventId("evt-1").setUploadNo("UPL001")
                .setObjectKey("users/7/original/a.jpg").setSizeBytes(200L).setContentType("image/jpeg")
                .setSignature("test-signature").setCallbackTime("2026-09-06T09:00:00Z");

        mvc.perform(post("/api/ossCallback/uploadCompleted").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data.photoNo").value("PHT001"));

        ArgumentCaptor<OssCallbackDto> captor = ArgumentCaptor.forClass(OssCallbackDto.class);
        verify(service).uploadCompleted(captor.capture());
        assertEquals("evt-1", captor.getValue().getEventId());
    }
}
