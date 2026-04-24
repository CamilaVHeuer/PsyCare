package com.camicompany.PsyCare.dto.patientDTO;

import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.model.PatientStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = """
        Update patient information.
            Send tutorId to reuse an existing tutor
            OR send tutor to create a new one.
            Do not send both.""")
public record PatientUpdateRequest(@Schema(example = "Manuel")
                                   String firstname,
                                   @Schema(example = "Suarez")
                                   String lastname,
                                   @Schema(example = "12345678")
                                   @Pattern(regexp = "\\d{7,8}", message = "Invalid DNI")
                                   String nationalId,
                                   @Schema(example = "1999-08-25")
                                   @PastOrPresent(message = "The date cannot be in the future")
                                   LocalDate birthDate,
                                   @Schema(example = "12345678")
                                   @Size(max = 20)
                                   String phone,
                                   Long tutorId,
                                   @Valid
                                   TutorCreateRequest tutor,
                                   @Schema(example = "2")
                                   Long insuranceId,
                                   @Schema(example = "12345678")
                                   String insuranceNumber) {
}
