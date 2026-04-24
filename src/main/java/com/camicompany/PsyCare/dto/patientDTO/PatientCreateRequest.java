package com.camicompany.PsyCare.dto.patientDTO;

import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.model.PatientStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
@Schema(description = """ 
        Create Patient.
        If patient is minor: 
        -send tutorId to reuse an existing tutor
        - or send tutor to create a new one (without both).
        """)
public record PatientCreateRequest(@Schema(example = "Manuel")
                                   @NotBlank(message = "The first name cannot be empty")
                                   String firstname,
                                   @Schema(example = "Suarez")
                                   @NotBlank(message = "The last name cannot be empty")
                                   String lastname,
                                   @Schema(example = "12345678")
                                   @NotBlank(message = "The DNI cannot be empty")
                                   @Pattern(regexp = "\\d{7,8}", message = "Invalid DNI")
                                   String nationalId,
                                   @Schema(example = "1999-08-25")
                                   @NotNull(message = "The birth date is required")
                                   @PastOrPresent(message = "The birth date cannot be in the future")
                                   LocalDate birthDate,
                                   @Schema(example = "12345678")
                                   @Size(max = 15, message = "The phone number cannot exceed 15 characters")
                                   String phone,
                                   Long tutorId,
                                   @Valid
                                   TutorCreateRequest tutor,
                                   @Schema(example = "1")
                                   Long insuranceId,
                                   @Schema(example = "12345678")
                                   String insuranceNumber) {
}
