package com.camicompany.PsyCare.dto.insuranceDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InsuranceCreateRequest (
        @NotBlank(message = "The name of the health insurance is required")
        @Schema(example = "IPS")
        String name,
        @Pattern(
                regexp = "^(20|23|24|27|30|33|34)-\\d{8}-\\d$",
                message = "The CUIT must have the format XX-XXXXXXXX-X"
        )
        @Size(min = 13, max = 13)
        @Schema(example = "20-12345678-1")
        String cuit){
}
