package com.camicompany.PsyCare.dto.appointmentDTO;

import com.camicompany.PsyCare.model.AppointmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppointmentCreateRequest(@NotNull
                                       @Future(message = "The appointment date cannot be in the past")
                                       @Schema(example = "2026-05-29T14:20:00")
                                       LocalDateTime appDateTime,
                                       @NotNull
                                       @Positive(message = "The price must be a positive number")
                                       @Schema(example = "10000.00")
                                       BigDecimal price,
                                       @Schema(example = "Manuel")
                                       String firstname,
                                       @Schema(example = "Sanchez")
                                       String lastname,
                                       @Schema(example = "1234567890")
                                       @Size(max = 15, message = "The phone number must not exceed 15 characters")
                                       String phone,
                                       @Schema(example = "1")
                                       Long patientId,
                                       @NotNull(message = "The appointment type is required")
                                       @Schema(example = "GENERAL")
                                       AppointmentType type) {
}
