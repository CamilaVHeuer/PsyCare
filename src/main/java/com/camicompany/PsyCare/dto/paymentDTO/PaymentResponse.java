package com.camicompany.PsyCare.dto.paymentDTO;

import com.camicompany.PsyCare.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        @Schema(example="2025-12-01")
        LocalDate payDate,
        @Schema(example = "20000")
        BigDecimal amount,
        @Schema(example = "Bank transfer")
        String payMethod,
        @Schema(example = "CREATED")
        PaymentStatus status,
        @Schema(example = "1")
        Long appointmentId) {
}
