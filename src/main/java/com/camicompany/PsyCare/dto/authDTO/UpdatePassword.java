package com.camicompany.PsyCare.dto.authDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePassword(
        @Schema(example = "user1OldPassword")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password must contain letters and numbers"
        )
        @NotBlank(message = "The old password is required")
        String oldPassword,
        @Schema(example = "user1NePassword")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password must contain letters and numbers"
        )
        @NotBlank (message = "The password is required") String newPassword
) {
}
