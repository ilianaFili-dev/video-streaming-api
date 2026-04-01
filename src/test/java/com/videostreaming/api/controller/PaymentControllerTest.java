package com.videostreaming.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videostreaming.api.config.SecurityConfig;
import com.videostreaming.api.dto.PaymentRequest;
import com.videostreaming.api.dto.PaymentResponse;
import com.videostreaming.api.exception.CreditCardNotFoundException;
import com.videostreaming.api.model.PaymentStatus;
import com.videostreaming.api.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentResponse buildPaymentResponse() {
        return new PaymentResponse(
                1L,
                "user1",
                new BigDecimal("99.99"),
                PaymentStatus.SUCCESS,
                "0366",
                LocalDateTime.now()
        );
    }

    @Test
    void processPayment_withValidData_returns201Created() throws Exception {
        PaymentRequest request = new PaymentRequest("4532015112830366", new BigDecimal("99.99"));
        PaymentResponse response = buildPaymentResponse();

        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(response);

        mockMvc.perform(
                post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.creditCardLastFour").value("0366"));
    }

    @Test
    void processPayment_withInvalidCardFormat_returns400BadRequest() throws Exception {
        String requestJson = "{"
                + "\"creditCardNumber\": \"12345\","
                + "\"amount\": \"99.99\""
                + "}";

        mockMvc.perform(
                post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void processPayment_withInvalidAmount_returns400BadRequest() throws Exception {
        String requestJson = "{"
                + "\"creditCardNumber\": \"4532015112830366\","
                + "\"amount\": \"0.00\""
                + "}";

        mockMvc.perform(
                post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void processPayment_withUnregisteredCard_returns404NotFound() throws Exception {
        PaymentRequest request = new PaymentRequest("5105105105105100", new BigDecimal("99.99"));

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenThrow(new CreditCardNotFoundException("Credit card is not registered to any user"));

        mockMvc.perform(
                post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }
}