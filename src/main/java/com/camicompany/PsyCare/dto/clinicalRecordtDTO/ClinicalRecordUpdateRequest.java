package com.camicompany.PsyCare.dto.clinicalRecordtDTO;

import io.swagger.v3.oas.annotations.media.Schema;


public record ClinicalRecordUpdateRequest(
                                          @Schema(example = "Anxiety")
                                          String reasonConsult,
                                          @Schema(example = "Chronic anxiety")
                                          String diagnosis,
                                          @Schema(example = "Does not sleep well")
                                          String obs,
                                          @Schema(example = "Anxiolytics")
                                          String medication) {
}
