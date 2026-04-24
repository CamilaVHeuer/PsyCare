package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.patientDTO.PatientCreateRequest;
import com.camicompany.PsyCare.dto.patientDTO.PatientResponse;
import com.camicompany.PsyCare.dto.patientDTO.PatientSummaryResponse;
import com.camicompany.PsyCare.dto.patientDTO.PatientUpdateRequest;
import com.camicompany.PsyCare.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Patients", description = "Operations for managing patients")
@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientServ;

    public PatientController(PatientService patientServ) {
        this.patientServ = patientServ;
    }

    @Operation(summary = "Get patient by ID", description = "Returns full patient details including tutor and insurance information.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientResponse.class))),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(
            @Parameter(description = "Patient ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(patientServ.getPatientById(id));
    }

    @Operation(summary = "Get all patients", description = "Returns a summary list of all patients (id, name, age, insurance).")
    @ApiResponse(responseCode = "200", description = "List returned successfully",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PatientSummaryResponse.class))))
    @GetMapping
    public ResponseEntity<List<PatientSummaryResponse>> getPatients() {
        return ResponseEntity.ok(patientServ.getAllPatients());
    }

    @Operation(
            summary = "Create a new patient",
            description = """
                    Creates a new patient. If the patient is a minor (under 18), a tutor is required. Provide either:
                    - `tutorId` to link an existing tutor, or
                    - `tutor` object to create a new one.
                    Do not provide both. Insurance is optional.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Patient created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or minor patient without tutor", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "tutorId or insuranceId not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "DNI already registered, or both tutorId and tutor provided", content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@RequestBody @Valid PatientCreateRequest patient) {
        PatientResponse patientCreated = patientServ.createPatient(patient);
        return ResponseEntity.created(URI.create("/api/v1/patients/" + patientCreated.patientId())).body(patientCreated);
    }

    @Operation(
            summary = "Update a patient",
            description = """
                    Partially updates patient information. All fields are optional.
                    If updating birthDate to a minor age, a tutor must be provided (via tutorId or tutor object).
                    Do not provide both tutorId and tutor.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or new birthDate makes patient minor without tutor", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Patient, tutorId or insuranceId not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "DNI already registered, or both tutorId and tutor provided", content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @Parameter(description = "Patient ID", example = "1", required = true) @PathVariable Long id,
            @RequestBody @Valid PatientUpdateRequest patient) {
        PatientResponse patientUpdated = patientServ.updatePatient(id, patient);
        return ResponseEntity.ok(patientUpdated);
    }

    @Operation(summary = "Reactivate a patient", description = "Sets the patient status back to ACTIVE. Cannot reactivate an already active patient.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient reactivated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientResponse.class))),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Patient is already active", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}/reactive")
    public ResponseEntity<PatientResponse> reactivePatient(
            @Parameter(description = "Patient ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(patientServ.reactivePatient(id));
    }

    @Operation(summary = "Discharge a patient", description = "Sets the patient status to DISCHARGED. Cannot discharge an already discharged patient.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient discharged successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientResponse.class))),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Patient is already discharged", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}/discharge")
    public ResponseEntity<PatientResponse> dischargePatient(
            @Parameter(description = "Patient ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(patientServ.dischargePatient(id));
    }
}
