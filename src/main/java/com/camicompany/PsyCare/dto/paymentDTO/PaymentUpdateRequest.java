package com.camicompany.PsyCare.dto.paymentDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentUpdateRequest(
        @PastOrPresent(message = "The payment registration date cannot be in the future")
        @Schema(example="2025-12-01")
        LocalDate payDate,
        @Positive(message = "The payment amount must be a positive value")
        @Schema(example = "20000")
        BigDecimal amount,
        @Schema(example = "Bank transfer")
        String payMethod) {
}
