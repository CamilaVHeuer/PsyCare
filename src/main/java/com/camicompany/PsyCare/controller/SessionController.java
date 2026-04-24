package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.sessionDTO.SessionCreateRequest;
import com.camicompany.PsyCare.dto.sessionDTO.SessionResponse;
import com.camicompany.PsyCare.dto.sessionDTO.SessionUpdateRequest;
import com.camicompany.PsyCare.service.SessionService;
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

@Tag(name = "Sessions", description = "Operations for managing therapy sessions")
@RestController
@RequestMapping("/api/v1")
public class SessionController {

    private final SessionService sessionServ;

    public SessionController(SessionService sessionServ) {
        this.sessionServ = sessionServ;
    }

    @Operation(summary = "Get session by ID", description = "Returns the details of a specific therapy session.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SessionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/sessions/{id}")
    public ResponseEntity<SessionResponse> getSession(
            @Parameter(description = "Session ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(sessionServ.getSessionById(id));
    }

    @Operation(summary = "Update a session", description = "Partially updates a session's date or notes. All fields are optional. The session date cannot be in the future.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SessionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., future date)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Session not found", content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/sessions/{id}")
    public ResponseEntity<SessionResponse> updateSession(
            @Parameter(description = "Session ID", example = "1", required = true) @PathVariable Long id,
            @RequestBody @Valid SessionUpdateRequest session) {
        return ResponseEntity.ok(sessionServ.updateSession(id, session));
    }

    @Operation(summary = "Create a session", description = "Creates a new therapy session linked to an existing clinical record. The session date cannot be in the future and notes are required.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SessionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., null date, future date, blank notes)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Clinical record not found", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/clinical-records/{clinicalRecordId}/sessions")
    public ResponseEntity<SessionResponse> createSession(
            @Parameter(description = "Clinical record ID", example = "1", required = true) @PathVariable Long clinicalRecordId,
            @RequestBody @Valid SessionCreateRequest session) {
        SessionResponse createdSession = sessionServ.createSession(clinicalRecordId, session);
        return ResponseEntity.created(URI.create("/api/v1/sessions/" + createdSession.sessionId())).body(createdSession);
    }
}
