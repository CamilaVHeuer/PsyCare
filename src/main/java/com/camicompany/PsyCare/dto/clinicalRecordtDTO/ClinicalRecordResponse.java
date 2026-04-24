package com.camicompany.PsyCare.dto.clinicalRecordtDTO;

import com.camicompany.PsyCare.dto.sessionDTO.SessionResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ClinicalRecordResponse( Long id,
                                      @Schema(description = "patient name", example = "Manuel")
                                     String firstname,
                                     @Schema(description = "patient lastname", example = "Suarez")
                                     String lastname,
                                     @Schema(example = "Chronic anxiety")
                                     String diagnosis,
                                     @Schema(example = "Does not sleep well")
                                     String obs,
                                     @Schema(example = "Anxiolytics")
                                     String medication,
                                     List<SessionResponse> sessions) {
}
