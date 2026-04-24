package com.camicompany.PsyCare.dto.clinicalRecordtDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

public record ClinicalRecordCreateRequest(
        @Schema(example = "Anxiety")
        @NotBlank(message = "The reason for consultation is required")
        String reasonConsult,
        @Schema(example = "Chronic anxiety")
        String diagnosis,
        @Schema(example = "Does not sleep well")
        String obs,
        @Schema(example = "Anxiolytics")
        String medication
         ) {
}
