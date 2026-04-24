package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordCreateRequest;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordResponse;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordUpdateRequest;
import com.camicompany.PsyCare.service.ClinicalRecordService;
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

@Tag(name = "Clinical Records", description = "Endpoints for managing patient clinical records")
@RestController
@RequestMapping("/api/v1")
public class ClinicalRecordController {

    private final ClinicalRecordService clinicalRecordServ;

    public ClinicalRecordController(ClinicalRecordService clinicalRecordServ) {
        this.clinicalRecordServ = clinicalRecordServ;
    }

    @Operation(
            summary = "Get clinical record by ID",
            description = "Returns the clinical record with the given ID, including all associated sessions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clinical record found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClinicalRecordResponse.class))),
            @ApiResponse(responseCode = "404", description = "Clinical record not found", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/clinical-records/{id}")
    public ResponseEntity<ClinicalRecordResponse> getClinicalRecords(
            @Parameter(description = "ID of the clinical record", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(clinicalRecordServ.getClinicalRecordById(id));
    }

    @Operation(
            summary = "Create a clinical record for a patient",
            description = "Creates a new clinical record associated with the given patient. Each patient can only have one clinical record."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Clinical record created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClinicalRecordResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request — missing or blank required fields", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "The patient already has a clinical record", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/patients/{patientId}/clinical-record")
    public ResponseEntity<ClinicalRecordResponse> createClinicalRecord(
            @Parameter(description = "ID of the patient", example = "1", required = true)
            @PathVariable Long patientId,
            @RequestBody @Valid ClinicalRecordCreateRequest clinicalRecord) {
        ClinicalRecordResponse createdRecord = clinicalRecordServ.createClinicalRecord(patientId, clinicalRecord);
        return ResponseEntity.created(URI.create("/api/v1/clinical-records/" + createdRecord.id())).body(createdRecord);
    }

    @Operation(
            summary = "Update a clinical record",
            description = "Partially updates a clinical record. All fields are optional — only the provided fields will be updated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clinical record updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClinicalRecordResponse.class))),
            @ApiResponse(responseCode = "404", description = "Clinical record not found", content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/clinical-records/{id}")
    public ResponseEntity<ClinicalRecordResponse> updateClinicalRecord(
            @Parameter(description = "ID of the clinical record to update", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody ClinicalRecordUpdateRequest clinicalRecord) {
        return ResponseEntity.ok(clinicalRecordServ.updateClinicalRecord(id, clinicalRecord));
    }
}
