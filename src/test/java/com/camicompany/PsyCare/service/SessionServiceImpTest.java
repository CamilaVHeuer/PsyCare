package com.camicompany.PsyCare.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.camicompany.PsyCare.dto.sessionDTO.SessionCreateRequest;
import com.camicompany.PsyCare.dto.sessionDTO.SessionResponse;
import com.camicompany.PsyCare.dto.sessionDTO.SessionUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.mapper.SessionMapper;
import com.camicompany.PsyCare.model.ClinicalRecord;
import com.camicompany.PsyCare.model.Session;
import com.camicompany.PsyCare.repository.ClinicalRecordRepository;
import com.camicompany.PsyCare.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceImpTest {
    @Mock private SessionRepository sessionRepo;
    @Mock private ClinicalRecordRepository clinicalRecordRepo;

    private SessionServiceImp sessionService;

    @BeforeEach
    void setUp() {
        SessionMapper mapper = new SessionMapper();
        sessionService = new SessionServiceImp(sessionRepo, mapper, clinicalRecordRepo);
    }

    @Test
    void shouldReturnSessionById() {
        ClinicalRecord clinicalRecord = new ClinicalRecord();
        clinicalRecord.setId(1L);
        Session session = new Session(1L, LocalDate.of(2026, 4, 15), "Notes", clinicalRecord);
        when(sessionRepo.findById(1L)).thenReturn(Optional.of(session));

        SessionResponse response = sessionService.getSessionById(1L);

        assertNotNull(response);
        assertEquals(1L, response.sessionId());
        assertEquals(LocalDate.of(2026, 4, 15), response.sessionDate());
        assertEquals("Notes", response.evolutionNotes());
        verify(sessionRepo).findById(1L);
    }

    @Test
    void shouldThrowResourceNotFoundWhenSessionDoesNotExist() {
        when(sessionRepo.findById(99L)).thenReturn(Optional.empty());
        var ex = assertThrows(ResourceNotFoundException.class, () -> sessionService.getSessionById(99L));
        assertEquals("Session not found with id 99", ex.getMessage());
        verify(sessionRepo).findById(99L);
    }

    @Test
    void shouldCreateSessionSuccessfully() {
        Long clinicalRecordId = 10L;
        ClinicalRecord clinicalRecord = new ClinicalRecord();
        clinicalRecord.setId(clinicalRecordId);
        clinicalRecord.setSessions(new ArrayList<>());

        SessionCreateRequest request = new SessionCreateRequest(LocalDate.of(2026, 4, 15), "Notes");
        Session savedSession = new Session(1L, request.sessionDate(), request.evolutionNotes(), clinicalRecord);

        when(clinicalRecordRepo.findById(clinicalRecordId)).thenReturn(Optional.of(clinicalRecord));
        when(sessionRepo.save(any(Session.class))).thenReturn(savedSession);

        SessionResponse response = sessionService.createSession(clinicalRecordId, request);

        assertNotNull(response);
        assertEquals(1L, response.sessionId());
        assertEquals(request.sessionDate(), response.sessionDate());
        assertEquals(request.evolutionNotes(), response.evolutionNotes());

        assertEquals(1, clinicalRecord.getSessions().size());
        Session sessionInList = clinicalRecord.getSessions().get(0);
        assertEquals(request.sessionDate(), sessionInList.getSessionDate());
        assertEquals(request.evolutionNotes(), sessionInList.getEvolutionNotes());
        assertEquals(clinicalRecord, sessionInList.getClinicalRecord());

        verify(clinicalRecordRepo).findById(clinicalRecordId);
        verify(sessionRepo).save(any(Session.class));

    }

    @Test
    void shouldThrowResourceNotFoundWhenClinicalRecordDoesNotExistOnCreate() {
        Long clinicalRecordId = 99L;
        SessionCreateRequest request = new SessionCreateRequest(LocalDate.of(2024, 4, 15), "Notes");
        when(clinicalRecordRepo.findById(clinicalRecordId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> sessionService.createSession(clinicalRecordId, request));
        assertEquals("Clinical record not found with id 99", ex.getMessage());
        verify(clinicalRecordRepo).findById(clinicalRecordId);
        verify(sessionRepo, never()).save(any());
    }

    @Test
    void shouldUpdateSessionSuccessfully() {
        ClinicalRecord clinicalRecord = new ClinicalRecord();
        clinicalRecord.setId(1L);
        Long sessionId = 3L;
        Session existingSession = new Session(sessionId, LocalDate.of(2026, 4, 10), "Old note", clinicalRecord);
        SessionUpdateRequest updateRequest = new SessionUpdateRequest(null, "Updated note");

        when(sessionRepo.findById(sessionId)).thenReturn(Optional.of(existingSession));
        when(sessionRepo.save(any(Session.class))).thenReturn(existingSession);

        SessionResponse response = sessionService.updateSession(sessionId, updateRequest);

        assertNotNull(response);
        assertEquals(sessionId, response.sessionId());
        assertEquals(existingSession.getSessionDate(), response.sessionDate());
        assertEquals(updateRequest.evolutionNotes(), response.evolutionNotes());

        assertEquals(LocalDate.of(2026, 4, 10), existingSession.getSessionDate());
        assertEquals("Updated note", existingSession.getEvolutionNotes());
        verify(sessionRepo).findById(sessionId);
        verify(sessionRepo).save(existingSession);
    }

    @Test
    void shouldThrowResourceNotFoundWhenSessionDoesNotExistOnUpdate() {
        Long sessionId = 99L;
        SessionUpdateRequest updateRequest = new SessionUpdateRequest(LocalDate.of(2024, 4, 20), "Nota");
        when(sessionRepo.findById(sessionId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> sessionService.updateSession(sessionId, updateRequest));
        assertEquals("Session not found with id 99", ex.getMessage());
        verify(sessionRepo).findById(sessionId);
        verify(sessionRepo, never()).save(any());
    }

    @Test
    void shouldSetClinicalRecordInSessionOnCreate() {
        Long clinicalRecordId = 10L;

        ClinicalRecord clinicalRecord = new ClinicalRecord();
        clinicalRecord.setId(clinicalRecordId);
        clinicalRecord.setSessions(new ArrayList<>());

        SessionCreateRequest request = new SessionCreateRequest(LocalDate.of(2024, 4, 20), "Notes");

        when(clinicalRecordRepo.findById(clinicalRecordId)).thenReturn(Optional.of(clinicalRecord));
        when(sessionRepo.save(any())).thenAnswer(inv -> {
            Session session = inv.getArgument(0);
            session.setId(1L);
            return session;
        });

        SessionResponse response = sessionService.createSession(clinicalRecordId, request);

        Session created = clinicalRecord.getSessions().get(0);
        assertEquals(clinicalRecord, created.getClinicalRecord());

        assertNotNull(response);
        assertEquals(1L, response.sessionId());
        assertEquals(request.sessionDate(), response.sessionDate());
        assertEquals(request.evolutionNotes(), response.evolutionNotes());
    }
}
