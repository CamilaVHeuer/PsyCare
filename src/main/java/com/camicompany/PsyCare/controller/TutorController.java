package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorResponse;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRelationRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRequest;
import com.camicompany.PsyCare.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Tutors", description = "Operations for managing tutors of minor patients")
@RestController
@RequestMapping("/api/v1/tutors")
public class TutorController {

    private final TutorService tutorServ;

    public TutorController(TutorService tutorServ) {
        this.tutorServ = tutorServ;
    }

    @Operation(summary = "Create a tutor", description = "Creates a new tutor. The CUIL must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tutor created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TutorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., blank fields, invalid CUIL format)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "CUIL already registered", content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<TutorResponse> createTutor(@RequestBody @Valid TutorCreateRequest tutor) {
        TutorResponse tutorCreated = tutorServ.createTutor(tutor);
        return ResponseEntity.created(URI.create("/api/v1/tutors/" + tutorCreated.id())).body(tutorCreated);
    }

    @Operation(summary = "Update a tutor", description = "Partially updates tutor information. All fields are optional. The CUIL must remain unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tutor updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TutorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Tutor not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "CUIL already registered", content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<TutorResponse> updateTutor(
            @Parameter(description = "Tutor ID", example = "1", required = true) @PathVariable Long id,
            @RequestBody @Valid TutorUpdateRequest tutor) {
        TutorResponse tutorUpdated = tutorServ.updateTutor(id, tutor);
        return ResponseEntity.ok(tutorUpdated);
    }

    @Operation(summary = "Update tutor-patient relation", description = "Updates the relationship type (e.g., MOTHER, FATHER, GUARDIAN) between a tutor and their associated patient.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relation updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TutorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., invalid relation type)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Tutor or patient not found", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}/relation")
    public ResponseEntity<TutorResponse> changeRelationTutorPatient(
            @Parameter(description = "Tutor ID", example = "1", required = true) @PathVariable Long id,
            @RequestBody @Valid TutorUpdateRelationRequest relation) {
        TutorResponse tutorUpdated = tutorServ.changeRelationTutorPatient(id, relation);
        return ResponseEntity.ok(tutorUpdated);
    }
}
