package com.camicompany.PsyCare.SecurityIntegration;

import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceCreateRequest;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceUpdateRequest;
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
public class InsuranceSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/insurances";

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────
    // 🔐 GET / — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void getAllInsurancesShouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 GET / — WITH TOKEN
    // ─────────────────────────────

    @Test
    void getAllInsurancesShouldReturn200WithValidToken() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ─────────────────────────────
    // 🔐 GET /{id} — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void getInsuranceByIdShouldReturn401WhenNoToken() throws Exception {
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
    void getInsuranceByIdShouldReturn404WithValidToken() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        mockMvc.perform(get(BASE_URL + "/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Insurance not found with id 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 POST / — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void createInsuranceShouldReturn401WhenNoToken() throws Exception {
        InsuranceCreateRequest request = new InsuranceCreateRequest("MEDIFE", "30-11223344-5");

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
    void createInsuranceShouldReturn201WithValidToken() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        InsuranceCreateRequest request = new InsuranceCreateRequest("MEDIFE", "30-11223344-5");

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("MEDIFE"))
                .andExpect(jsonPath("$.cuit").value("30-11223344-5"));
    }

    // ─────────────────────────────
    // 🔐 PATCH /{id} — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void updateInsuranceShouldReturn401WhenNoToken() throws Exception {
        InsuranceUpdateRequest request = new InsuranceUpdateRequest("MEDIFE", null);

        mockMvc.perform(patch(BASE_URL + "/1")
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
    void getAllInsurancesShouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get(BASE_URL)
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
