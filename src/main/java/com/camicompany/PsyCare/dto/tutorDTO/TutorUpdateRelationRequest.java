package com.camicompany.PsyCare.dto.tutorDTO;

import com.camicompany.PsyCare.model.TutorRelation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record TutorUpdateRelationRequest(@Schema(example = "FATHER")
                                         @NotNull(message = "Relation is required")
                                         TutorRelation relation) {
}
