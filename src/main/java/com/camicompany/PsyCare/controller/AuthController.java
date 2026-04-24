package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.authDTO.AuthResponse;
import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.dto.authDTO.MessageResponse;
import com.camicompany.PsyCare.dto.authDTO.UpdatePassword;
import com.camicompany.PsyCare.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Authentication and password management operations")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authServ;

    public AuthController(AuthService authService) {
        this.authServ = authService;
    }

    @Operation(
            summary = "Login",
            description = "Authenticates the user with username and password. Returns a JWT token to be used in subsequent requests."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful — JWT token returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (blank username or password)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody @Valid LoginRequest loginUserDTO) {
        AuthResponse authResponse = authServ.loginUser(loginUserDTO);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(
            summary = "Update password",
            description = "Updates the password of the currently authenticated user. Requires a valid JWT token. " +
                    "The new password must be at least 8 characters and contain both letters and numbers."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (password too short, missing numbers, blank field)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Current password is incorrect or user is not authenticated", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Authenticated user not found", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/update-password")
    public ResponseEntity<MessageResponse> updatePassword(@RequestBody @Valid UpdatePassword newPassword) {
        MessageResponse response = authServ.updatePassword(newPassword);
        return ResponseEntity.ok(response);
    }
}
