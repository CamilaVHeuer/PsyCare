package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.sessionDTO.SessionCreateRequest;
import com.camicompany.PsyCare.dto.sessionDTO.SessionResponse;
import com.camicompany.PsyCare.dto.sessionDTO.SessionUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.mapper.SessionMapper;
import com.camicompany.PsyCare.model.ClinicalRecord;
import com.camicompany.PsyCare.model.Session;
import com.camicompany.PsyCare.repository.ClinicalRecordRepository;
import com.camicompany.PsyCare.repository.SessionRepository;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceImp implements SessionService {

    private final SessionRepository sessionRepo;
    private final SessionMapper sessionMapper;
    private final ClinicalRecordRepository clinicalRecordRepo;

    public SessionServiceImp(SessionRepository sessionRepo, SessionMapper sessionMapper, ClinicalRecordRepository clinicalRecordRepo) {
        this.sessionRepo = sessionRepo;
        this.sessionMapper = sessionMapper;
        this.clinicalRecordRepo = clinicalRecordRepo;
    }

    @Override
    public SessionResponse getSessionById(Long id) {
        Session session = sessionRepo.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Session not found with id " + id));
        return sessionMapper.toResponse(session);
    }

    @Override
    public SessionResponse createSession(Long clinicalRecordId, SessionCreateRequest session) {
        ClinicalRecord clinicalRecord = clinicalRecordRepo.findById(clinicalRecordId).orElseThrow(
                ()-> new ResourceNotFoundException("Clinical record not found with id " + clinicalRecordId)
        );
            Session sessionEntity = sessionMapper.toEntity(session);
            sessionEntity.setClinicalRecord(clinicalRecord);
            clinicalRecord.getSessions().add(sessionEntity);
            //session is owner
            Session savedSession = sessionRepo.save(sessionEntity);
            return sessionMapper.toResponse(savedSession);
    }

    @Override
    public SessionResponse updateSession(Long id, SessionUpdateRequest session) {
        Session sessionEntity = sessionRepo.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Session not found with id " + id));
        if(session.sessionDate()!= null) sessionEntity.setSessionDate(session.sessionDate());
        if(session.evolutionNotes()!= null) sessionEntity.setEvolutionNotes(session.evolutionNotes());
        Session updatedSession = sessionRepo.save(sessionEntity);
        return sessionMapper.toResponse(updatedSession);
    }
}
