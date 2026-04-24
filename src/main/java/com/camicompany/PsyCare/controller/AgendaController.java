package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Agenda", description = "Operations for querying available appointment slots")
@RestController
@RequestMapping("/api/v1/agenda")
public class AgendaController {

    private final AgendaService agendaServ;

    public AgendaController(AgendaService agendaServ) {
        this.agendaServ = agendaServ;
    }

    @Operation(
            summary = "Get available slots for a given date",
            description = "Returns a list of available time slots for the specified date. " +
                    "Weekends are not available. Slots are generated based on the configured " +
                    "working hours and session duration, excluding already booked appointments."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of available slots returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(type = "string", format = "date-time")),
                            examples = @ExampleObject(
                                    value = "[\"2026-11-03T13:00:00\", \"2026-11-03T13:40:00\", \"2026-11-03T14:20:00\"]"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The requested date falls on a weekend or is otherwise invalid",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/available-slots")
    public ResponseEntity<List<LocalDateTime>> getAvailableSlots(
            @Parameter(
                    description = "Date to query available slots for (format: yyyy-MM-dd). Must be a weekday.",
                    example = "2026-11-03",
                    required = true
            )
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(agendaServ.getAvailableSlots(date));
    }
}
