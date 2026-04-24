package com.camicompany.PsyCare.dto.patientDTO;

import com.camicompany.PsyCare.model.PatientStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Change patient status ")
public record PatientChangeStatusRequest(
        @Schema(example = "DISCHARGED")
        @NotNull(message = "Status is required")
        PatientStatus status) {
}
