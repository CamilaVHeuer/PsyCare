package com.camicompany.PsyCare.SecurityIntegration;

import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRelationRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRequest;
import com.camicompany.PsyCare.model.TutorRelation;
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
public class TutorSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/tutors";

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────
    // 🔐 POST / — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void createTutorShouldReturn401WhenNoToken() throws Exception {
        TutorCreateRequest request = new TutorCreateRequest(
                "Rosa", "Diaz", "99887766", "27-99887766-4", TutorRelation.MOTHER
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
    void createTutorShouldReturn201WithValidToken() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        TutorCreateRequest request = new TutorCreateRequest(
                "Rosa", "Diaz", "99887766", "27-99887766-4", TutorRelation.MOTHER
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstname").value("Rosa"))
                .andExpect(jsonPath("$.lastname").value("Diaz"))
                .andExpect(jsonPath("$.relation").value("MOTHER"));
    }

    // ─────────────────────────────
    // 🔐 PATCH /{id} — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void updateTutorShouldReturn401WhenNoToken() throws Exception {
        TutorUpdateRequest request = new TutorUpdateRequest(
                "Carlos", null, null, null
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
    // 🔐 PATCH /{id} — WITH TOKEN
    // ─────────────────────────────

    @Test
    void updateTutorShouldReturn404WithValidTokenWhenNotFound() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        TutorUpdateRequest request = new TutorUpdateRequest(
                "Carlos", null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/99999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 PUT /{id}/relation — WITHOUT TOKEN
    // ─────────────────────────────

    @Test
    void updateTutorRelationShouldReturn401WhenNoToken() throws Exception {
        TutorUpdateRelationRequest request = new TutorUpdateRelationRequest(TutorRelation.LEGAL_GUARDIAN);

        mockMvc.perform(put(BASE_URL + "/1/relation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐 PUT /{id}/relation — WITH TOKEN
    // ─────────────────────────────

    @Test
    void updateTutorRelationShouldReturn404WithValidTokenWhenNotFound() throws Exception {
        String token = obtainAccessToken("test_user", "test_password123");

        TutorUpdateRelationRequest request = new TutorUpdateRelationRequest(TutorRelation.LEGAL_GUARDIAN);

        mockMvc.perform(put(BASE_URL + "/99999/relation")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────
    // 🔐  INVALID TOKEN
    // ─────────────────────────────

    @Test
    void createTutorShouldReturn401WithInvalidToken() throws Exception {
        TutorCreateRequest request = new TutorCreateRequest(
                "Rosa", "Diaz", "99887766", "27-99887766-4", TutorRelation.MOTHER
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer invalid.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
