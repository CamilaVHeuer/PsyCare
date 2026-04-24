package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentCreateRequest;
import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentResponse;
import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentUpdateRequest;
import com.camicompany.PsyCare.service.AppointmentService;
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

@Tag(name = "Appointments", description = "Operations for managing appointments")
@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentServ;

    public AppointmentController(AppointmentService appointmentServ) {
        this.appointmentServ = appointmentServ;
    }

    @Operation(summary = "Get appointment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointment found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(
            @Parameter(description = "Appointment ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(appointmentServ.getAppointmentById(id));
    }

    @Operation(
            summary = "Create a new appointment",
            description = """
                    Creates a new appointment. To associate a patient, provide either:
                    - `patientId` to link an existing patient, or
                    - `firstname`, `lastname` and `phone` to register ad-hoc patient data.
                    Do not provide both. The time slot must be available (no other SCHEDULED appointment at the same date/time).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Appointment created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (missing fields, past date, negative price, etc.)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Patient not found when patientId is provided", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Time slot already booked, or both patientId and patient data provided", content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @RequestBody @Valid AppointmentCreateRequest appointment) {
        AppointmentResponse appointmentCreated = appointmentServ.createAppointment(appointment);
        return ResponseEntity.created(URI.create("/api/v1/appointments/" + appointmentCreated.id())).body(appointmentCreated);
    }

    @Operation(
            summary = "Update an appointment",
            description = """
                    Partially updates an appointment. All fields are optional.
                    To reassign a patient, provide either `patientId` or patient data fields — not both.
                    If a new date/time is provided, the slot must be available.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointment updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Appointment or patient not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Time slot already booked, or both patientId and patient data provided", content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @Parameter(description = "Appointment ID", example = "1", required = true) @PathVariable Long id,
            @RequestBody @Valid AppointmentUpdateRequest appointment) {
        return ResponseEntity.ok(appointmentServ.updateAppointment(id, appointment));
    }

    @Operation(summary = "Cancel an appointment", description = "Sets the appointment status to CANCELLED. Cannot cancel an already cancelled appointment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointment cancelled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Appointment is already cancelled", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @Parameter(description = "Appointment ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(appointmentServ.cancelAppointment(id));
    }

    @Operation(summary = "Mark appointment as attended", description = "Sets the appointment status to ATTENDED. Cannot mark an already attended appointment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointment marked as attended",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Appointment is already attended", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}/mark-as-attended")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @Parameter(description = "Appointment ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(appointmentServ.markAsAttended(id));
    }

    @Operation(summary = "Mark appointment as no-show", description = "Sets the appointment status to NO_SHOW. Cannot mark an already no-show appointment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Appointment marked as no-show",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Appointment is already marked as no-show", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}/mark-as-no-show")
    public ResponseEntity<AppointmentResponse> markAsNoShow(
            @Parameter(description = "Appointment ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(appointmentServ.markAsNoShow(id));
    }
}
