package com.app.controller;

import com.app.pojo.dto.TestDto;
import com.app.pojo.vo.TestVo;
import com.app.service.TestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 测试控制器测试
 *
 * @author yanlei
 * @since 2026-09-05
 */
class TestControllerTest {

    private MockMvc mockMvc;
    private TestService testService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        testService = mock(TestService.class);
        TestController testController = new TestController();
        ReflectionTestUtils.setField(testController, "testService", testService);
        mockMvc = MockMvcBuilders.standaloneSetup(testController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateTest() throws Exception {
        TestDto.SaveOrUpdate request = new TestDto.SaveOrUpdate().setName("张三");
        when(testService.save(request)).thenReturn(true);

        mockMvc.perform(post("/api/test/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data").value(true));

        verify(testService).save(request);
    }

    @Test
    void shouldDeleteTest() throws Exception {
        TestDto.Delete request = new TestDto.Delete().setId(1L);
        when(testService.delete(request)).thenReturn(true);

        mockMvc.perform(post("/api/test/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data").value(true));

        verify(testService).delete(request);
    }

    @Test
    void shouldUpdateTest() throws Exception {
        TestDto.SaveOrUpdate request = new TestDto.SaveOrUpdate().setId(1L).setName("李四");
        when(testService.update(request)).thenReturn(true);

        mockMvc.perform(post("/api/test/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data").value(true));

        verify(testService).update(request);
    }

    @Test
    void shouldGetTestById() throws Exception {
        when(testService.getTestById(1L)).thenReturn(new TestVo().setId(1L).setName("张三"));

        mockMvc.perform(get("/api/test/getById").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("张三"));
    }

    @Test
    void shouldListTests() throws Exception {
        when(testService.listTest()).thenReturn(List.of(
                new TestVo().setId(1L).setName("张三"),
                new TestVo().setId(2L).setName("李四")
        ));

        mockMvc.perform(get("/api/test/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("张三"));
    }
}