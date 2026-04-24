package com.camicompany.PsyCare.dto.tutorDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TutorUpdateRequest(
        @Schema(example = "Julio")
        String firstname,
        @Schema(example = "Suarez")
        String lastname,
        @Schema(example = "12345678")
        @Size(max = 20)
        String phone,
        @Schema(example = "20-12345678-1")
        @Pattern(
                regexp = "^(20|23|24|27|30|33|34)-\\d{8}-\\d$",
                message = "CUIL must have the format XX-XXXXXXXX-X"
        )
        @Size(min = 13, max = 13)
        String cuil){
}
