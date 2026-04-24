package com.camicompany.PsyCare.dto.authDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example="user1")
        @NotBlank(message = "The username is required") String username,
        @Schema(example="user1Password")
        @NotBlank (message = "The password is required") String password
) {
}
