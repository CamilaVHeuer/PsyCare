package com.camicompany.PsyCare.dto.sessionDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record SessionResponse(@Schema(example="1")
                              Long sessionId,
                              @Schema(example="2025-12-01")
                              LocalDate sessionDate,
                              String evolutionNotes) {
}
