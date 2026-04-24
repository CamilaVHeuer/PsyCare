package com.camicompany.PsyCare.dto.sessionDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record SessionUpdateRequest(
        @PastOrPresent(message = "The date cannot be in the future")
        @Schema(example="2025-12-01")
        LocalDate sessionDate,
        String evolutionNotes
) {
}
