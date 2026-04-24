package com.camicompany.PsyCare.SecurityIntegration;

import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.dto.sessionDTO.SessionCreateRequest;
import com.camicompany.PsyCare.dto.sessionDTO.SessionUpdateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SessionSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String SESSIONS_URL = "/api/v1/sessions";
    private static final String CLINICAL_RECORDS_URL = "/api/v1/clinical-records";

    // ─────────────────────────────
    // 🔐 GET /sessions/{id} — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void getSessionByIdShouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get(SESSIONS_URL + "/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 GET /sessions/{id} — WITH TOKEN
    // ─────────────────────────────

    @Test
    void getSessionByIdShouldReturn404WithValidToken() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        mockMvc.perform(get(SESSIONS_URL + "/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Session not found with id 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 POST /clinical-records/{id}/sessions — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void createSessionShouldReturn401WhenNoToken() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest(
                LocalDate.of(2026, 4, 10), "First follow-up session"
        );

        mockMvc.perform(post(CLINICAL_RECORDS_URL + "/1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 POST /clinical-records/{id}/sessions — WITH TOKEN
    // ─────────────────────────────

    @Test
    void createSessionShouldReturn404WithValidTokenWhenClinicalRecordNotFound() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        SessionCreateRequest request = new SessionCreateRequest(
                LocalDate.of(2026, 4, 10), "First follow-up session"
        );

        mockMvc.perform(post(CLINICAL_RECORDS_URL + "/99999/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Clinical record not found with id 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 PATCH /sessions/{id} — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void updateSessionShouldReturn401WhenNoToken() throws Exception {
        SessionUpdateRequest request = new SessionUpdateRequest(
                LocalDate.of(2026, 4, 10), "Updated notes"
        );

        mockMvc.perform(patch(SESSIONS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 PATCH /sessions/{id} — WITH TOKEN
    // ─────────────────────────────

    @Test
    void updateSessionShouldReturn404WithValidTokenWhenNotFound() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        SessionUpdateRequest request = new SessionUpdateRequest(
                LocalDate.of(2026, 4, 10), "Updated notes"
        );

        mockMvc.perform(patch(SESSIONS_URL + "/99999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Session not found with id 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 INVALID TOKEN
    // ─────────────────────────────

    @Test
    void getSessionByIdShouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get(SESSIONS_URL + "/1")
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
