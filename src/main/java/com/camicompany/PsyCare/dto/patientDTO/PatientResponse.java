package com.camicompany.PsyCare.dto.patientDTO;

import com.camicompany.PsyCare.dto.tutorDTO.TutorResponse;
import com.camicompany.PsyCare.model.PatientStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
@Schema(description = "Patient information")
public record PatientResponse(
        @Schema(example = "1")
        Long patientId,
        @Schema(example = "Manuel")
        String firstname,
        @Schema(example = "Suarez")
        String lastname,
        @Schema(example = "12345678")
        String nationalId,
        @Schema(example = "1999-08-25")
        LocalDate birthDate,
        Integer age,
        @Schema(example = "12345678")
        String phone,
        @Schema(example = "ACTIVE")
        PatientStatus status,
        TutorResponse tutor,
        @Schema(example = "IPS")
        String insuranceName,
        @Schema(example = "12345678")
        String insuranceNumber
) {

}
