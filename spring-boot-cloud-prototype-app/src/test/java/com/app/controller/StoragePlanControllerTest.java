package com.app.controller;

import com.app.pojo.vo.StoragePlanVo;
import com.app.service.StoragePlanService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoragePlanControllerTest {
    @Test
    void shouldListActivePlans() throws Exception {
        StoragePlanService service = mock(StoragePlanService.class);
        when(service.listActivePlans()).thenReturn(List.of(new StoragePlanVo.ActivePlan()
                .setPlanCode("BASIC").setPlanName("基础套餐").setCapacityBytes(1024L)));
        StoragePlanController controller = new StoragePlanController();
        ReflectionTestUtils.setField(controller, "storagePlanService", service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/storagePlan/listActivePlans"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data[0].planCode").value("BASIC"));
        verify(service).listActivePlans();
    }
}
