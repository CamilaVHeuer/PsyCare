package com.camicompany.PsyCare.SecurityIntegration;
import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.dto.authDTO.UpdatePassword;
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


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthSecurityIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/auth";

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────────────────────
    // 🔐 LOGIN
    // ─────────────────────────────────────────────

    @Test
    void loginShouldReturnTokenWithValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("test_user", "test_password123");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void loginShouldReturn401WithInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("test_user", "wrongPassword");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Bad credentials"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────────────────────
    // 🔐 UPDATE PASSWORD (PROTECTED ENDPOINT)
    // ─────────────────────────────────────────────

    @Test
    void updatePasswordShouldWorkWithValidToken() throws Exception {

        // 🔹 1. login → get token
        String token = obtainAccessToken("test_user", "test_password123");

        UpdatePassword request = new UpdatePassword("test_password123", "newPassword456");

        // 🔹 2. use token
        mockMvc.perform(put(BASE_URL + "/update-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }

    @Test
    void updatePasswordShouldReturn401WhenNoToken() throws Exception {

        UpdatePassword request = new UpdatePassword("test_password123", "newPassword456");

        mockMvc.perform(put(BASE_URL + "/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Access denied: authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePasswordShouldReturn401WithInvalidToken() throws Exception {

        UpdatePassword request = new UpdatePassword("test_password123", "newPassword456");

        mockMvc.perform(put(BASE_URL + "/update-password")
                        .header("Authorization", "Bearer invalid.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid or expired token"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePasswordShouldReturn401WhenOldPasswordIsIncorrect() throws Exception {

        String token = obtainAccessToken("test_user", "test_password123");

        UpdatePassword request = new UpdatePassword("wrongOldPassword123", "newPassword456");

        mockMvc.perform(put(BASE_URL + "/update-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────────────────────
    // 🔧 HELPER
    // ─────────────────────────────────────────────

    private String obtainAccessToken(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);

        String response = mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("token").asText();
    }
}



