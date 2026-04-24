package com.camicompany.PsyCare.dto.insuranceDTO;

import io.swagger.v3.oas.annotations.media.Schema;

public record InsuranceResponse(
        @Schema(example = "1")
        Long id,
        @Schema(example = "IPS")
        String name,
        @Schema(example = "20-12345678-1")
        String cuit){
}
