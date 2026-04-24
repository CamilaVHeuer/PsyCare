package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.security.config.filter.JwtTokenValidator;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.camicompany.PsyCare.dto.sessionDTO.SessionCreateRequest;
import com.camicompany.PsyCare.dto.sessionDTO.SessionResponse;
import com.camicompany.PsyCare.dto.sessionDTO.SessionUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SessionController.class, excludeAutoConfiguration =  SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class SessionControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private SessionService sessionServ;
    @MockitoBean private JwtTokenValidator jwtTokenValidator;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void getSessionShouldReturn200() throws Exception {
        SessionResponse response = new SessionResponse(1L, LocalDate.of(2024, 4, 15), "Notes");
        when(sessionServ.getSessionById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/sessions/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(1L))
                .andExpect(jsonPath("$.sessionDate").value("2024-04-15"))
                .andExpect(jsonPath("$.evolutionNotes").value("Notes"));
        verify(sessionServ).getSessionById(1L);
    }

    @Test
    void getSessionShouldReturn404() throws Exception {
        when(sessionServ.getSessionById(99L)).thenThrow(new ResourceNotFoundException("Session not found with id 99"));

        mockMvc.perform(get("/api/v1/sessions/99")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Session not found with id 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createSessionShouldReturn201() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest(LocalDate.of(2024, 4, 15), "New note");
        SessionResponse response = new SessionResponse(2L, request.sessionDate(), request.evolutionNotes());
        when(sessionServ.createSession(eq(10L), any(SessionCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/clinical-records/10/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/sessions/2"))
                .andExpect(jsonPath("$.sessionId").value(2L))
                .andExpect(jsonPath("$.sessionDate").value("2024-04-15"))
                .andExpect(jsonPath("$.evolutionNotes").value("New note"));
        verify(sessionServ).createSession(eq(10L), any(SessionCreateRequest.class));
    }

    @Test
    void createSessionShouldReturn404WhenClinicalRecordNotFound() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest(LocalDate.of(2024, 4, 15), "Note");
        when(sessionServ.createSession(eq(99L), any(SessionCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Clinical record not found with id 99"));

        mockMvc.perform(post("/api/v1/clinical-records/99/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Clinical record not found with id 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createSessionShouldReturn400WhenDateIsNull() throws Exception {
        String json = """
            {
                "sessionDate": null,
                "evolutionNotes": "Nota"
            }
            """;
        mockMvc.perform(post("/api/v1/clinical-records/10/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("The date cannot be empty")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createSessionShouldReturn400WhenDateIsFuture() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        SessionCreateRequest request = new SessionCreateRequest(futureDate, "Note");
        mockMvc.perform(post("/api/v1/clinical-records/10/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("The date cannot be in the future")))
                .andExpect(jsonPath("$.timestamp").exists());

    }

    @Test
    void createSessionShouldReturn400WhenNotesIsBlank() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest(LocalDate.of(2024, 4, 15), "");
        mockMvc.perform(post("/api/v1/clinical-records/10/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("You must enter at least one note for the record")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateSessionShouldReturn200() throws Exception {
        SessionUpdateRequest updateRequest = new SessionUpdateRequest(LocalDate.of(2024, 4, 20), "Updated note");
        SessionResponse response = new SessionResponse(3L, updateRequest.sessionDate(), updateRequest.evolutionNotes());
        when(sessionServ.updateSession(eq(3L), any(SessionUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/sessions/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(3L))
                .andExpect(jsonPath("$.sessionDate").value("2024-04-20"))
                .andExpect(jsonPath("$.evolutionNotes").value("Updated note"));
        verify(sessionServ).updateSession(eq(3L), any(SessionUpdateRequest.class));
    }

    @Test
    void updateSessionShouldAllowEmptyNotes() throws Exception {
        SessionUpdateRequest updateRequest = new SessionUpdateRequest(
                LocalDate.of(2024, 4, 20),
                ""
        );

        when(sessionServ.updateSession(eq(3L), any()))
                .thenReturn(new SessionResponse(3L, updateRequest.sessionDate(), ""));

        mockMvc.perform(patch("/api/v1/sessions/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(3L))
                .andExpect(jsonPath("$.sessionDate").value("2024-04-20"))
                .andExpect(jsonPath("$.evolutionNotes").value(""));
        verify(sessionServ).updateSession(eq(3L), any(SessionUpdateRequest.class));
    }

    @Test
    void updateSessionShouldAllowPartialUpdate() throws Exception {
        SessionUpdateRequest updateRequest = new SessionUpdateRequest(null, "Only notes updated");
        when(sessionServ.updateSession(eq(3L), any()))
                .thenReturn(new SessionResponse(3L, LocalDate.of(2024,4,15), "Only notes updated"));

        mockMvc.perform(patch("/api/v1/sessions/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(3L))
                .andExpect(jsonPath("$.sessionDate").value("2024-04-15"))
                .andExpect(jsonPath("$.evolutionNotes").value("Only notes updated"));
        verify(sessionServ).updateSession(eq(3L), any(SessionUpdateRequest.class));
    }
    @Test
    void updateSessionShouldReturn404() throws Exception {
        SessionUpdateRequest updateRequest = new SessionUpdateRequest(LocalDate.of(2024, 4, 20), "Note");
        when(sessionServ.updateSession(eq(99L), any(SessionUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Session not found with id 99"));

        mockMvc.perform(patch("/api/v1/sessions/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Session not found with id 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateSessionShouldReturn400WhenDateIsFuture() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        SessionUpdateRequest updateRequest = new SessionUpdateRequest(futureDate, "Note");
        mockMvc.perform(patch("/api/v1/sessions/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("The date cannot be in the future")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
