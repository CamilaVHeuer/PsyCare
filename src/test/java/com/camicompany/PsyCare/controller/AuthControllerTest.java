package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.authDTO.AuthResponse;
import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.dto.authDTO.MessageResponse;
import com.camicompany.PsyCare.dto.authDTO.UpdatePassword;
import com.camicompany.PsyCare.security.config.filter.JwtTokenValidator;
import com.camicompany.PsyCare.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;



@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration =  SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AuthService authService;

    @MockitoBean private JwtTokenValidator jwtTokenValidator;

    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/auth";


    @Test
    void loginShouldReturn200() throws Exception {
        LoginRequest request = new LoginRequest("user1", "user1Password");
        AuthResponse response = new AuthResponse("jwt-token");

        when(authService.loginUser(request)).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).loginUser(request);
    }

    @Test
    void loginShouldReturn400WhenUsernameIsBlank() throws Exception {
        LoginRequest request = new LoginRequest("", "password123");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The username is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void loginShouldReturn400WhenPasswordIsBlank() throws Exception {
        LoginRequest request = new LoginRequest("user1", "");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The password is required")))
                .andExpect(jsonPath("$.timestamp").exists());

    }

    @Test
    void loginShouldReturn401WhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest("user1", "wrongPass");

        when(authService.loginUser(any()))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePasswordShouldReturn200() throws Exception {
        UpdatePassword request = new UpdatePassword("oldPass123", "newPass123");
        MessageResponse response = new MessageResponse("Password updated successfully");

        when(authService.updatePassword(request)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
            verify(authService).updatePassword(request);
    }

    @Test
    void updatePasswordShouldReturn400WhenPasswordTooShort() throws Exception {
        UpdatePassword request = new UpdatePassword("short123", "new123");

        mockMvc.perform(put(BASE_URL + "/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("Password must be at least 8 characters")))
                .andExpect(jsonPath("$.timestamp").exists());

    }

    @Test
    void updatePasswordShouldReturn400WhenPasswordWithoutNumbers() throws Exception {
        UpdatePassword request = new UpdatePassword("oldpassword", "newpassword");

        mockMvc.perform(put(BASE_URL + "/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("Password must contain letters and numbers")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePasswordShouldReturn400WhenOldPasswordIsNull() throws Exception {
        UpdatePassword request = new UpdatePassword(null, "newPass123");

        mockMvc.perform(put(BASE_URL + "/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The old password is required")))
                .andExpect(jsonPath("$.timestamp").exists());

    }

}
