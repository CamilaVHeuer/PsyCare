package com.camicompany.PsyCare.SecurityIntegration;

import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentCreateRequest;
import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentUpdateRequest;
import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.model.AppointmentType;
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
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AppointmentSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/appointments";

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────
    // 🔐 GET /{id} — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void getAppointmentByIdShouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 GET /{id} — WITH TOKEN
    // ─────────────────────────────

    @Test
    void getAppointmentByIdShouldReturn404WithValidToken() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        mockMvc.perform(get(BASE_URL + "/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 POST / — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void createAppointmentShouldReturn401WhenNoToken() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2026, 12, 1, 13, 0),
                BigDecimal.valueOf(15000),
                "Laura", "Gonzalez", "987654321",
                null,
                AppointmentType.GENERAL
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 POST / — WITH TOKEN
    // ─────────────────────────────

    @Test
    void createAppointmentShouldReturn201WithValidToken() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2026, 12, 1, 13, 0),
                BigDecimal.valueOf(15000),
                "Laura", "Gonzalez", "987654321",
                null,
                AppointmentType.GENERAL
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    // ─────────────────────────────
    // 🔐 PATCH /{id} — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void updateAppointmentShouldReturn401WhenNoToken() throws Exception {
        AppointmentUpdateRequest request = new AppointmentUpdateRequest(
                LocalDateTime.of(2026, 12, 5, 15, 0),
                null, null, null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 PUT /{id}/cancel — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void cancelAppointmentShouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1/cancel"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 INVALID TOKEN
    // ─────────────────────────────

    @Test
    void getAppointmentByIdShouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1")
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
