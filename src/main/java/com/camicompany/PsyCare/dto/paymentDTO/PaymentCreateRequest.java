package com.camicompany.PsyCare.dto.paymentDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentCreateRequest(
        @NotNull
        @PastOrPresent(message = "The payment registration date cannot be in the future")
        @Schema(example="2026-04-01")
        LocalDate payDate,
        @NotNull
        @Positive(message = "The payment amount must be a positive value")
        @Schema(example = "20000")
        BigDecimal amount,
        @NotBlank(message = "The payment method is required")
        @Schema(example = "Bank transfer")
        String payMethod){

}
