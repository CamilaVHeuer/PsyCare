package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.model.PaymentStatus;
import com.camicompany.PsyCare.security.config.filter.JwtTokenValidator;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentCreateRequest;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentResponse;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class, excludeAutoConfiguration =  SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)

public class PaymentControllerTest {
    @Autowired private MockMvc mockMvc;

    @MockitoBean private PaymentService paymentServ;
    @MockitoBean private JwtTokenValidator jwtTokenValidator;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1";

    @Test
    void getPaymentByIdShouldReturn200() throws Exception {
        PaymentResponse response = new PaymentResponse(1L, LocalDate.of(2024, 4, 15), BigDecimal.valueOf(30000), "Bank transfer", PaymentStatus.CREATED,10L);
        when(paymentServ.getPaymentById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/payments/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.payDate").value("2024-04-15"))
                .andExpect(jsonPath("$.amount").value(30000))
                .andExpect(jsonPath("$.payMethod").value("Bank transfer"))
                .andExpect(jsonPath("$.status").value(PaymentStatus.CREATED.toString()))
                .andExpect(jsonPath("$.appointmentId").value(10L));
        verify(paymentServ).getPaymentById(1L);
    }

    @Test
    void getPaymentByIdShouldReturn404() throws Exception {
        when(paymentServ.getPaymentById(99L)).thenThrow(new ResourceNotFoundException("Payment not found with id: 99"));

        mockMvc.perform(get(BASE_URL + "/payments/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Payment not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getPaymentsByAppointmentIdShouldReturn200() throws Exception {
        PaymentResponse response = new PaymentResponse(1L, LocalDate.of(2024, 4, 10), BigDecimal.valueOf(10000),
                "Cash", PaymentStatus.CREATED, 10L);

        when(paymentServ.getPaymentsByAppointmentId(10L)).thenReturn(List.of(response));

        mockMvc.perform(get(BASE_URL + "/appointments/10/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].payDate").value("2024-04-10"))
                .andExpect(jsonPath("$[0].amount").value(10000))
                .andExpect(jsonPath("$[0].payMethod").value("Cash"))
                .andExpect(jsonPath("$[0].status").value(PaymentStatus.CREATED.toString()))
                .andExpect(jsonPath("$[0].appointmentId").value(10L));
        verify(paymentServ).getPaymentsByAppointmentId(10L);
    }

    @Test
    void getPaymentsByAppointmentIdShouldReturn404() throws Exception {
        when(paymentServ.getPaymentsByAppointmentId(99L)).thenThrow(new ResourceNotFoundException("Appointment not found with id: 99"));

        mockMvc.perform(get(BASE_URL + "/appointments/99/payments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getPaymentsByDateRangeShouldReturn200() throws Exception {
        PaymentResponse resp1 = new PaymentResponse(1L, LocalDate.of(2024, 4, 10), BigDecimal.valueOf(10000), "Cash",PaymentStatus.CREATED,  10L);
        PaymentResponse resp2 = new PaymentResponse(2L, LocalDate.of(2024, 4, 20), BigDecimal.valueOf(20000), "Bank transfer",PaymentStatus.CREATED,  10L);
        when(paymentServ.getAllPaymentsByDate(LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 30)))
                .thenReturn(List.of(resp1, resp2));

        mockMvc.perform(get(BASE_URL + "/payments")
                        .param("startDate", "2024-04-01")
                        .param("endDate", "2024-04-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].payDate").value("2024-04-10"))
                .andExpect(jsonPath("$[0].amount").value(10000))
                .andExpect(jsonPath("$[0].payMethod").value("Cash"))
                .andExpect(jsonPath("$[0].status").value(PaymentStatus.CREATED.toString()))
                .andExpect(jsonPath("$[0].appointmentId").value(10L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].payDate").value("2024-04-20"))
                .andExpect(jsonPath("$[1].amount").value(20000))
                .andExpect(jsonPath("$[1].payMethod").value("Bank transfer"))
                .andExpect(jsonPath("$[1].status").value(PaymentStatus.CREATED.toString()))
                .andExpect(jsonPath("$[1].appointmentId").value(10L));
        verify(paymentServ).getAllPaymentsByDate(LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 30));
    }

    @Test
    void getPaymentsByDateRangeShouldReturn400WhenDateIsInvalid() throws Exception {
        mockMvc.perform(get(BASE_URL + "/payments")
                        .param("startDate", "invalid-date")
                        .param("endDate", "2024-04-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid parameter format"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void registerPaymentShouldReturn201() throws Exception {
        PaymentCreateRequest request = new PaymentCreateRequest(LocalDate.of(2024, 4, 15), BigDecimal.valueOf(30000), "Bank transfer");
        PaymentResponse response = new PaymentResponse(1L, request.payDate(), request.amount(), request.payMethod(), PaymentStatus.CREATED, 10L);
        when(paymentServ.registerPayment(eq(10L), any(PaymentCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/appointments/10/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/payments/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.payDate").value("2024-04-15"))
                .andExpect(jsonPath("$.amount").value(30000))
                .andExpect(jsonPath("$.payMethod").value("Bank transfer"))
                .andExpect(jsonPath("$.status").value(PaymentStatus.CREATED.toString()))
                .andExpect(jsonPath("$.appointmentId").value(10L));
        verify(paymentServ).registerPayment(eq(10L), any(PaymentCreateRequest.class));
    }

    @Test
    void registerPaymentShouldReturn404WhenAppointmentNotFound() throws Exception {
        PaymentCreateRequest request = new PaymentCreateRequest(LocalDate.of(2024, 4, 15), BigDecimal.valueOf(30000), "Bank transfer");
        when(paymentServ.registerPayment(eq(99L), any(PaymentCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Appointment not found with id: 99"));

        mockMvc.perform(post(BASE_URL + "/appointments/99/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void registerPaymentShouldReturn400WhenDateIsFuture() throws Exception {
        PaymentCreateRequest request = new PaymentCreateRequest(LocalDate.now().plusDays(2), BigDecimal.valueOf(30000), "Bank transfer");
        mockMvc.perform(post( BASE_URL + "/appointments/10/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("The payment registration date cannot be in the future")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void registerPaymentShouldReturn400WhenAmountIsNegative() throws Exception {
        PaymentCreateRequest request = new PaymentCreateRequest(LocalDate.now(), BigDecimal.valueOf(-100), "Bank transfer");
        mockMvc.perform(post(BASE_URL + "/appointments/10/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("The payment amount must be a positive value")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePaymentShouldReturn200() throws Exception {
        PaymentUpdateRequest updateRequest = new PaymentUpdateRequest(LocalDate.of(2024, 4, 20), BigDecimal.valueOf(10000), "Bank transfer");
        PaymentResponse response = new PaymentResponse(1L, updateRequest.payDate(), updateRequest.amount(), updateRequest.payMethod(), PaymentStatus.CREATED,  10L);
        when(paymentServ.updatePayment(eq(1L), any(PaymentUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.payDate").value("2024-04-20"))
                .andExpect(jsonPath("$.amount").value(10000))
                .andExpect(jsonPath("$.payMethod").value("Bank transfer"))
                .andExpect(jsonPath("$.status").value(PaymentStatus.CREATED.toString()))
                .andExpect(jsonPath("$.appointmentId").value(10L));
        verify(paymentServ).updatePayment(eq(1L), any(PaymentUpdateRequest.class));
    }

    @Test
    void updatePaymentShouldReturn404() throws Exception {
        PaymentUpdateRequest updateRequest = new PaymentUpdateRequest(LocalDate.of(2024, 4, 20), BigDecimal.valueOf(10000), "Bank transfer");
        when(paymentServ.updatePayment(eq(99L), any(PaymentUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Payment not found with id: 99"));

        mockMvc.perform(patch(BASE_URL + "/payments/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Payment not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePaymentShouldReturn400WhenDateIsFuture() throws Exception {
        PaymentUpdateRequest updateRequest = new PaymentUpdateRequest(LocalDate.now().plusDays(2), BigDecimal.valueOf(10000), "Bank transfer");
        mockMvc.perform(patch(BASE_URL + "/payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("The payment registration date cannot be in the future")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePaymentShouldReturn400WhenAmountIsNegative() throws Exception {
        PaymentUpdateRequest updateRequest = new PaymentUpdateRequest(LocalDate.now(), BigDecimal.valueOf(-100), "Bank transfer");
        mockMvc.perform(patch(BASE_URL + "/payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("The payment amount must be a positive value")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void cancelPaymentShouldReturn200() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L, LocalDate.of(2024, 4, 15), BigDecimal.valueOf(30000), "Bank transfer", PaymentStatus.CANCELED,
                10L);
        when(paymentServ.cancelPayment(1L)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/payments/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.payDate").value("2024-04-15"))
                .andExpect(jsonPath("$.amount").value(30000))
                .andExpect(jsonPath("$.payMethod").value("Bank transfer"))
                .andExpect(jsonPath("$.status").value(PaymentStatus.CANCELED.toString()))
                .andExpect(jsonPath("$.appointmentId").value(10L));
        verify(paymentServ).cancelPayment(1L);
    }

    @Test
    void cancelPaymentShouldReturn404() throws Exception {
        when(paymentServ.cancelPayment(99L)).thenThrow(new ResourceNotFoundException("Payment not found with id: 99"));

        mockMvc.perform(put(BASE_URL + "/payments/99/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Payment not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
