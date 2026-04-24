package com.camicompany.PsyCare.SecurityIntegration;

import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentCreateRequest;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentUpdateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PaymentSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1";

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────
    // 🔐 GET /payments/{id} — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void getPaymentByIdShouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/payments/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 GET /payments/{id} — WITH TOKEN
    // ─────────────────────────────

    @Test
    void getPaymentByIdShouldReturn404WithValidToken() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        mockMvc.perform(get(BASE_URL + "/payments/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Payment not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 GET /payments — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void getPaymentsByDateRangeShouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/payments")
                        .param("startDate", "2026-04-01")
                        .param("endDate", "2026-04-30"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 GET /payments — WITH TOKEN
    // ─────────────────────────────

    @Test
    void getPaymentsByDateRangeShouldReturn200WithValidToken() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        mockMvc.perform(get(BASE_URL + "/payments")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", "2026-04-01")
                        .param("endDate", "2026-04-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ─────────────────────────────
    // 🔐 GET /appointments/{id}/payments — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void getPaymentsByAppointmentShouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/appointments/1/payments"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 POST /appointments/{id}/payments — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void registerPaymentShouldReturn401WhenNoToken() throws Exception {
        PaymentCreateRequest request = new PaymentCreateRequest(
                LocalDate.of(2026, 4, 20), BigDecimal.valueOf(20000), "Bank transfer"
        );

        mockMvc.perform(post(BASE_URL + "/appointments/1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 POST /appointments/{id}/payments — WITH TOKEN
    // ─────────────────────────────

    @Test
    void registerPaymentShouldReturn404WithValidTokenWhenAppointmentNotFound() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        PaymentCreateRequest request = new PaymentCreateRequest(
                LocalDate.of(2026, 4, 20), BigDecimal.valueOf(20000), "Bank transfer"
        );

        mockMvc.perform(post(BASE_URL + "/appointments/99999/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 PATCH /payments/{id} — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void updatePaymentShouldReturn401WhenNoToken() throws Exception {
        PaymentUpdateRequest request = new PaymentUpdateRequest(
                null, BigDecimal.valueOf(15000), null
        );

        mockMvc.perform(patch(BASE_URL + "/payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 PUT /payments/{id}/cancel — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void cancelPaymentShouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(put(BASE_URL + "/payments/1/cancel"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 INVALID TOKEN
    // ─────────────────────────────

    @Test
    void getPaymentByIdShouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/payments/1")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid or expired token"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔧 HELPER
    // ─────────────────────────────

    private String obtainAccessToken(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("token").asText();
    }
}
