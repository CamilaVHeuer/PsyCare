package com.camicompany.PsyCare.SecurityIntegration;

import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordCreateRequest;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordUpdateRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ClinicalRecordSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1";

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────
    // 🔐 GET /clinical-records/{id} — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void getClinicalRecordByIdShouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/clinical-records/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 GET /clinical-records/{id} — WITH TOKEN
    // ─────────────────────────────

    @Test
    void getClinicalRecordByIdShouldReturn404WithValidToken() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        mockMvc.perform(get(BASE_URL + "/clinical-records/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("ClinicalRecord not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 POST /patients/{id}/clinical-record — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void createClinicalRecordShouldReturn401WhenNoToken() throws Exception {
        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest("Anxiety", "Chronic anxiety", null, null);

        mockMvc.perform(post(BASE_URL + "/patients/1/clinical-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 POST /patients/{id}/clinical-record — WITH TOKEN
    // ─────────────────────────────

    @Test
    void createClinicalRecordShouldReturn404WithValidTokenWhenPatientNotFound() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest("Anxiety", "Chronic anxiety", null, null);

        mockMvc.perform(post(BASE_URL + "/patients/99999/clinical-record")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 PATCH /clinical-records/{id} — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void updateClinicalRecordShouldReturn401WhenNoToken() throws Exception {
        ClinicalRecordUpdateRequest request = new ClinicalRecordUpdateRequest(null, "Updated diagnosis", null, null);

        mockMvc.perform(patch(BASE_URL + "/clinical-records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 INVALID TOKEN
    // ─────────────────────────────

    @Test
    void getClinicalRecordByIdShouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/clinical-records/1")
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

