package com.app.controller;

import com.app.pojo.vo.StorageVo;
import com.app.security.AuthenticatedUser;
import com.app.security.UserContextHolder;
import com.app.service.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StorageControllerTest {
    @AfterEach
    void clear() { UserContextHolder.clear(); }

    @Test
    void shouldGetOverviewForCurrentUser() throws Exception {
        StorageService service = mock(StorageService.class);
        when(service.getOverview(7L)).thenReturn(new StorageVo.Overview().setTotalCapacityBytes(300L).setRemainingBytes(100L));
        MockMvc mockMvc = mockMvc(service);
        mockMvc.perform(get("/api/storage/getOverview")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCapacityBytes").value(300))
                .andExpect(jsonPath("$.data.remainingBytes").value(100));
        verify(service).getOverview(7L);
    }

    @Test
    void shouldListEntitlementsForCurrentUser() throws Exception {
        StorageService service = mock(StorageService.class);
        when(service.listEntitlements(7L)).thenReturn(List.of(new StorageVo.Entitlement()
                .setSourceType("PURCHASE").setNameSnapshot("购买套餐").setCapacityBytes(200L)));
        MockMvc mockMvc = mockMvc(service);
        mockMvc.perform(get("/api/storage/listEntitlements")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sourceType").value("PURCHASE"))
                .andExpect(jsonPath("$.data[0].nameSnapshot").value("购买套餐"));
        verify(service).listEntitlements(7L);
    }

    private MockMvc mockMvc(StorageService service) {
        UserContextHolder.set(new AuthenticatedUser(7L, "Asia/Shanghai", "token-id"));
        StorageController controller = new StorageController();
        ReflectionTestUtils.setField(controller, "storageService", service);
        return MockMvcBuilders.standaloneSetup(controller).build();
    }
}
