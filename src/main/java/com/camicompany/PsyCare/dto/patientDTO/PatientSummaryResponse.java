package com.camicompany.PsyCare.dto.patientDTO;

import io.swagger.v3.oas.annotations.media.Schema;



@Schema(description = "Patient information")
public record PatientSummaryResponse(
        @Schema(example = "1")
        Long patientId,
        @Schema(example = "Manuel")
        String firstname,
        @Schema(example = "Suarez")
        String lastname,
        Integer age,
        @Schema(example = "IPS")
        String insuranceName

) {

}
