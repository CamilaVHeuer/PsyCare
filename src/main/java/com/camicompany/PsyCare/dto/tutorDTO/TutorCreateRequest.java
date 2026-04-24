package com.camicompany.PsyCare.dto.tutorDTO;

import com.camicompany.PsyCare.model.TutorRelation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TutorCreateRequest(@NotBlank(message = "The tutor's first name is required")
                                 @Schema(example = "José")
                                 String firstname,

                                 @NotBlank(message = "The tutor's last name is required")
                                 @Schema(example = "Suarez")
                                 String lastname,

                                 @NotBlank(message = "The tutor's phone number is required")
                                 @Size(max = 20)
                                 @Schema(example = "12345678")
                                 String phone,
                                 @NotBlank(message = "The tutor's CUIL is required")
                                 @Pattern(
                                         regexp = "^(20|23|24|27|30|33|34)-\\d{8}-\\d$",
                                         message = "CUIL must have the format XX-XXXXXXXX-X"
                                 )
                                 @Size(min = 13, max = 13)
                                 @Schema(example = "20-12345678-1")
                                 String cuil,

                                 @NotNull(message = "Relation is required")
                                 @Schema(example = "FATHER")
                                 TutorRelation relation) {
}
