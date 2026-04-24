package com.camicompany.PsyCare.dto.sessionDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;


import java.time.LocalDate;

public record SessionCreateRequest(
        @NotNull(message = "The date cannot be empty")
        @PastOrPresent(message = "The date cannot be in the future")
        @Schema(example="2025-12-01")
        LocalDate sessionDate,
        @NotBlank(message = "You must enter at least one note for the record")
        String evolutionNotes
) {
}
