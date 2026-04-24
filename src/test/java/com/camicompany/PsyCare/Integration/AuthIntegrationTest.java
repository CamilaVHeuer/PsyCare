package com.camicompany.PsyCare.Integration;

import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.dto.authDTO.UpdatePassword;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class AuthIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/auth";

    // ── POST /login ──────────────────────────────────────────────────────────

    @Test
    void loginShouldReturn200WithValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("test_user", "test_password123");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void loginShouldReturn401WhenInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("test_user", "wrongPassword123");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void loginShouldReturn400WhenUsernameIsBlank() throws Exception {
        LoginRequest request = new LoginRequest("", "test_password123");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The username is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void loginShouldReturn400WhenPasswordIsBlank() throws Exception {
        LoginRequest request = new LoginRequest("test_user", "");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The password is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PUT /update-password ─────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "test_user")
    void updatePasswordShouldReturn200WithValidRequest() throws Exception {
        UpdatePassword request = new UpdatePassword("test_password123", "newPassword456");

        mockMvc.perform(put(BASE_URL + "/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }

    @Test
    @WithMockUser(username = "test_user")
    void updatePasswordShouldReturn401WhenOldPasswordIsIncorrect() throws Exception {
        UpdatePassword request = new UpdatePassword("wrongOldPass123", "newPassword456");

        mockMvc.perform(put(BASE_URL + "/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePasswordShouldReturn400WhenOldPasswordIsNull() throws Exception {
        UpdatePassword request = new UpdatePassword(null, "newPassword456");

        mockMvc.perform(put(BASE_URL + "/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The old password is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePasswordShouldReturn400WhenPasswordWithoutNumbers() throws Exception {
        UpdatePassword request = new UpdatePassword("test_password123", "newpassword");

        mockMvc.perform(put(BASE_URL + "/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("Password must contain letters and numbers")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePasswordShouldReturn400WhenPasswordTooShort() throws Exception {
        UpdatePassword request = new UpdatePassword("test_password123", "new1");

        mockMvc.perform(put(BASE_URL + "/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("Password must be at least 8 characters")))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
