package com.camicompany.PsyCare.dto.appointmentDTO;

import com.camicompany.PsyCare.model.AppointmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppointmentUpdateRequest(
        @Future
        @Schema(example = "2026-05-29T14:20:00")
        LocalDateTime appDateTime,
        @Positive
        @Schema(example = "10000.00")
        BigDecimal price,
        @Schema(example = "Manuel")
        String firstname,
        @Schema(example = "Garcia")
        String lastname,
        @Schema(example = "1234567890")
        @Size(max = 15, message = "The phone number must not exceed 15 characters")
        String phone,
        @Schema(example = "1")
        Long patientId,
        @Schema(example = "GENERAL")
        AppointmentType type) {
}
