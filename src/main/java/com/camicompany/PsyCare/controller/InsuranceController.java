package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceCreateRequest;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceResponse;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceUpdateRequest;
import com.camicompany.PsyCare.service.InsuranceService;
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

@Tag(name = "Insurances", description = "Operations for managing health insurances")
@RestController
@RequestMapping("/api/v1/insurances")
public class InsuranceController {

    private final InsuranceService insuranceServ;

    public InsuranceController(InsuranceService insuranceServ) {
        this.insuranceServ = insuranceServ;
    }

    @Operation(summary = "Get all insurances", description = "Returns the list of all registered health insurances.")
    @ApiResponse(responseCode = "200", description = "List returned successfully",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = InsuranceResponse.class))))
    @GetMapping
    public ResponseEntity<List<InsuranceResponse>> getInsurances() {
        return ResponseEntity.ok(insuranceServ.getAllInsurances());
    }

    @Operation(summary = "Get insurance by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insurance found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InsuranceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Insurance not found", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}")
    public ResponseEntity<InsuranceResponse> getInsuranceById(
            @Parameter(description = "Insurance ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(insuranceServ.getInsuranceById(id));
    }

    @Operation(summary = "Get insurance by name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insurance found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InsuranceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Insurance not found", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/name/{name}")
    public ResponseEntity<InsuranceResponse> getInsuranceByName(
            @Parameter(description = "Insurance name", example = "IPS", required = true) @PathVariable String name) {
        return ResponseEntity.ok(insuranceServ.getInsuranceByName(name));
    }

    @Operation(summary = "Create a new insurance", description = "Registers a new health insurance. The CUIT must follow the format XX-XXXXXXXX-X and must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Insurance created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InsuranceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (blank name, invalid CUIT format)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "An insurance with this CUIT already exists", content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<InsuranceResponse> createInsurance(@RequestBody @Valid InsuranceCreateRequest insurance) {
        InsuranceResponse insuranceCreated = insuranceServ.createInsurance(insurance);
        return ResponseEntity.created(URI.create("/api/v1/insurances/" + insuranceCreated.id())).body(insuranceCreated);
    }

    @Operation(summary = "Update an insurance", description = "Partially updates an insurance. All fields are optional.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insurance updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InsuranceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (invalid CUIT format)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Insurance not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "An insurance with this CUIT already exists", content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<InsuranceResponse> updateInsurance(
            @Parameter(description = "Insurance ID", example = "1", required = true) @PathVariable Long id,
            @RequestBody @Valid InsuranceUpdateRequest insurance) {
        return ResponseEntity.ok(insuranceServ.updateInsurance(id, insurance));
    }
}
