package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.authDTO.AuthResponse;
import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.dto.authDTO.MessageResponse;
import com.camicompany.PsyCare.dto.authDTO.UpdatePassword;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.model.UserApp;
import com.camicompany.PsyCare.repository.UserAppRepository;
import com.camicompany.PsyCare.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImpTest {

    @Mock private UserAppRepository userAppRepo;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private JwtUtils jwtUtils;

    @Mock private AuthenticationManager authenticationManager;

    @Mock private Authentication authentication;

    private AuthServiceImp authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImp(userAppRepo, passwordEncoder, jwtUtils, authenticationManager);
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("user", "password1234");

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(jwtUtils.generateToken(authentication))
                .thenReturn("mocked-jwt-token");

        AuthResponse response = authService.loginUser(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.token());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateToken(authentication);
    }

    @Test
    void shouldThrowExceptionWhenLoginFails() {
        LoginRequest request = new LoginRequest("user", "wrong-password1234");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        var ex = assertThrows(BadCredentialsException.class, () -> authService.loginUser(request));
        assertEquals("Bad credentials", ex.getMessage());

        verify(authenticationManager).authenticate(any());
        verify(jwtUtils, never()).generateToken(any());
    }


    @Test
    void shouldUpdatePasswordSuccessfully() {
        UpdatePassword request = new UpdatePassword("oldPass1234", "newPass1234");

        UserApp user = new UserApp();
        user.setUsername("user");
        user.setPassword("encoded-old-pass1234");

        // mock security context
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userAppRepo.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass1234", "encoded-old-pass1234")).thenReturn(true);
        when(passwordEncoder.encode("newPass1234")).thenReturn("encoded-new-pass1234");

        MessageResponse response = authService.updatePassword(request);

        assertNotNull(response);
        assertEquals("Password updated successfully", response.message());

        assertEquals("encoded-new-pass1234", user.getPassword());

        verify(userAppRepo).save(user);
    }

    @Test
    void shouldThrowExceptionWhenOldPasswordIsIncorrect() {
        UpdatePassword request = new UpdatePassword("wrongOldPass1234", "newPass1234");

        UserApp user = new UserApp();
        user.setUsername("user");
        user.setPassword("encoded-old-pass1234");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userAppRepo.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPass1234", "encoded-old-pass1234")).thenReturn(false);

        var ex = assertThrows(BadCredentialsException.class,
                () -> authService.updatePassword(request));

        assertEquals("Current password is incorrect", ex.getMessage());

        verify(userAppRepo, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        UpdatePassword request = new UpdatePassword("oldPass1234", "newPass1234");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userAppRepo.findByUsername("user")).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class,
                () -> authService.updatePassword(request));
        assertEquals("User not found", ex.getMessage());

        verify(userAppRepo, never()).save(any());
    }

    // =========================
    // ENCRYPT PASSWORD
    // =========================

    @Test
    void shouldEncryptPassword() {
        when(passwordEncoder.encode("plain"))
                .thenReturn("encoded");

        String result = authService.encryptPassword("plain");

        assertEquals("encoded", result);

        verify(passwordEncoder).encode("plain");
    }
}
