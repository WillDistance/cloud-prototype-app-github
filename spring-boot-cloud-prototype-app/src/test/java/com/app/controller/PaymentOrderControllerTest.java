package com.app.controller;

import com.app.pojo.dto.CreatePaymentOrderDto;
import com.app.pojo.vo.PaymentOrderVo;
import com.app.service.PaymentOrderService;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentOrderControllerTest {
    @Test
    void shouldCreateOrder() throws Exception {
        PaymentOrderService service = mock(PaymentOrderService.class);
        when(service.createOrder(any(CreatePaymentOrderDto.class))).thenReturn(new PaymentOrderVo()
                .setOrderNo("PO001").setPlanCodeSnapshot("BASIC").setAmountCent(9900L)
                .setExpireTime(LocalDateTime.of(2026, 9, 6, 12, 30)));
        PaymentOrderController controller = new PaymentOrderController();
        ReflectionTestUtils.setField(controller, "paymentOrderService", service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/paymentOrder/createOrder").contentType("application/json")
                        .content(JsonMapper.builder().build().writeValueAsString(new CreatePaymentOrderDto()
                                .setPlanCode("BASIC").setPlanVersion(3).setClientRequestId("req-001"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data.orderNo").value("PO001"))
                .andExpect(jsonPath("$.data.amountCent").value(9900));
        verify(service).createOrder(any(CreatePaymentOrderDto.class));
    }

    @Test
    void shouldGetOrderByQueryParameter() throws Exception {
        PaymentOrderService service = mock(PaymentOrderService.class);
        when(service.getOrder("PO001")).thenReturn(new PaymentOrderVo().setOrderNo("PO001").setStatus("PENDING"));
        PaymentOrderController controller = new PaymentOrderController();
        ReflectionTestUtils.setField(controller, "paymentOrderService", service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/paymentOrder/getOrder").param("orderNo", "PO001"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.errorCode").value("E00000"))
                .andExpect(jsonPath("$.data.orderNo").value("PO001"));
        verify(service).getOrder("PO001");
    }
}
