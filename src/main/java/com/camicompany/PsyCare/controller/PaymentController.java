package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.paymentDTO.PaymentCreateRequest;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentResponse;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentUpdateRequest;
import com.camicompany.PsyCare.service.PaymentService;
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
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Payments", description = "Operations for managing payments")
@RestController
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentServ;

    public PaymentController(PaymentService paymentServ) {
        this.paymentServ = paymentServ;
    }

    @Operation(summary = "Get payments by date range", description = "Returns all payments registered between startDate and endDate (inclusive).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PaymentResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid date format", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2025-01-01", required = true) @RequestParam LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2025-12-31", required = true) @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(paymentServ.getAllPaymentsByDate(startDate, endDate));
    }

    @Operation(summary = "Get payment by ID", description = "Returns the details of a specific payment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @Parameter(description = "Payment ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(paymentServ.getPaymentById(id));
    }

    @Operation(summary = "Get payments by appointment", description = "Returns all payments associated with a specific appointment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PaymentResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/appointments/{appointmentId}/payments")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByAppointmentId(
            @Parameter(description = "Appointment ID", example = "1", required = true) @PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentServ.getPaymentsByAppointmentId(appointmentId));
    }

    @Operation(summary = "Register a payment", description = "Creates a new payment linked to an existing appointment.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., future date, negative amount, blank payment method)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/appointments/{appointmentId}/payments")
    public ResponseEntity<PaymentResponse> registerPayment(
            @Parameter(description = "Appointment ID", example = "1", required = true) @PathVariable Long appointmentId,
            @RequestBody @Valid PaymentCreateRequest payment) {
        PaymentResponse paymentCreated = paymentServ.registerPayment(appointmentId, payment);
        return ResponseEntity.created(URI.create("/api/v1/payments/" + paymentCreated.id())).body(paymentCreated);
    }

    @Operation(summary = "Update a payment", description = "Partially updates payment information. All fields are optional.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Payment not found", content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/payments/{id}")
    public ResponseEntity<PaymentResponse> updatePayment(
            @Parameter(description = "Payment ID", example = "1", required = true) @PathVariable Long id,
            @RequestBody @Valid PaymentUpdateRequest payment) {
        return ResponseEntity.ok(paymentServ.updatePayment(id, payment));
    }

    @Operation(summary = "Cancel a payment", description = "Sets the payment status to CANCELLED. Cannot cancel an already cancelled payment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment cancelled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Payment is already cancelled", content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/payments/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @Parameter(description = "Payment ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(paymentServ.cancelPayment(id));
    }

}
