package com.camicompany.PsyCare.dto.tutorDTO;

import com.camicompany.PsyCare.model.TutorRelation;
import io.swagger.v3.oas.annotations.media.Schema;


public record TutorResponse(
        @Schema(example = "1")
        Long id,
        @Schema(example = "José")
        String firstname,
        @Schema(example = "Suarez")
        String lastname,
        @Schema(example = "12345678")
        String phone,
        @Schema(example = "20-12345678-1")
        String cuil,
        @Schema(example = "Father")
        TutorRelation relation
) {
}
