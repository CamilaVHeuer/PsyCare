package com.camicompany.PsyCare.service;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordCreateRequest;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordResponse;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordUpdateRequest;
import com.camicompany.PsyCare.exception.*;
import com.camicompany.PsyCare.model.ClinicalRecord;
import com.camicompany.PsyCare.model.Patient;
import com.camicompany.PsyCare.model.Session;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import com.camicompany.PsyCare.mapper.ClinicalRecordMapper;
import com.camicompany.PsyCare.mapper.SessionMapper;
import com.camicompany.PsyCare.repository.ClinicalRecordRepository;
import com.camicompany.PsyCare.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClinicalRecordServiceImpTest {

    private ClinicalRecordService clinicalRecordService;

    @Mock private ClinicalRecordRepository clinicalRecordRepo;
    @Mock private PatientRepository patientRepo;

    @BeforeEach
    void setUp() {
        SessionMapper sessionMapper = new SessionMapper();
        ClinicalRecordMapper clinicalRecordMapper = new ClinicalRecordMapper(sessionMapper);
        clinicalRecordService = new ClinicalRecordServiceImp(
                                        clinicalRecordRepo,
                                        clinicalRecordMapper,
                                        patientRepo);
    }


    @Test
    void shouldReturnClinicalRecordById() {
        // Arrange
        Long id = 1L;
        ClinicalRecord clinicalRecord = createClinicalRecord(id, "reason", "diagnosis", "obs", "medication");
        Patient patient = new Patient();
        patient.setFirstname("Juan");
        patient.setLastname("Pérez");
        clinicalRecord.setPatient(patient);
        LocalDate date = LocalDate.now();
        Session session = createSession(1L, date, "evolution notes");
        clinicalRecord.setSessions(List.of(session));
        when(clinicalRecordRepo.findById(id)).thenReturn(Optional.of(clinicalRecord));

        // Act
        ClinicalRecordResponse response = clinicalRecordService.getClinicalRecordById(id);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Juan", response.firstname());
        assertEquals("Pérez", response.lastname());
        assertEquals("diagnosis", response.diagnosis());
        assertEquals("obs", response.obs());
        assertEquals("medication", response.medication());
        assertNotNull(response.sessions());
        assertEquals(1L, response.sessions().size());
        assertEquals(1L, response.sessions().get(0).sessionId());
        assertEquals(date, response.sessions().get(0).sessionDate());
        assertEquals("evolution notes", response.sessions().get(0).evolutionNotes());

        verify(clinicalRecordRepo).findById(id);
    }

    @Test
    void shouldReturnClinicalRecordWithoutSessions() {
        Long id = 1L;

        ClinicalRecord clinicalRecord = createClinicalRecord(id, "reason", "diagnosis", "obs", "medication");

        Patient patient = new Patient();
        patient.setFirstname("Juan");
        patient.setLastname("Pérez");

        clinicalRecord.setPatient(patient);
        clinicalRecord.setSessions(List.of()); // empty

        when(clinicalRecordRepo.findById(id)).thenReturn(Optional.of(clinicalRecord));

        ClinicalRecordResponse response = clinicalRecordService.getClinicalRecordById(id);

        assertNotNull(response.sessions());
        assertTrue(response.sessions().isEmpty());
    }

    @Test
    void shouldThrowResourceNotFoundWhenClinicalRecordDoesNotExist() {
        Long id = 99L;
        when(clinicalRecordRepo.findById(id)).thenReturn(Optional.empty());
        var ex = assertThrows(ResourceNotFoundException.class, () -> clinicalRecordService.getClinicalRecordById(id));
        assertEquals("ClinicalRecord not found with id: " + id, ex.getMessage());
        verify(clinicalRecordRepo).findById(id);
    }



    @Test
    void shouldCreateClinicalRecordSuccessfully() {
        // Arrange
        Long patientId = 1L;
        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest("reason", "diagnosis", "obs", "medication");
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setFirstname("Juan");
        patient.setLastname("Pérez");
        when(patientRepo.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientRepo.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ClinicalRecordResponse response = clinicalRecordService.createClinicalRecord(patientId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Juan", response.firstname());
        assertEquals("Pérez", response.lastname());
        assertEquals("diagnosis", response.diagnosis());
        assertEquals("obs", response.obs());
        assertEquals("medication", response.medication());
        verify(patientRepo).findById(patientId);
        verify(patientRepo).save(patient);
        assertNotNull(patient.getClinicalRecord());
        assertEquals(patient, patient.getClinicalRecord().getPatient());
    }

    @Test
    void shouldThrowResourceNotFoundWhenCreatingClinicalRecordForNonexistentPatient() {
        Long patientId = 99L;
        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest("reason", "diagnosis", "obs", "medication");
        when(patientRepo.findById(patientId)).thenReturn(Optional.empty());
        var ex = assertThrows(ResourceNotFoundException.class, () -> clinicalRecordService.createClinicalRecord(patientId, request));
        assertEquals("Patient not found with id: " + patientId, ex.getMessage());
        verify(patientRepo).findById(patientId);
    }

    @Test
    void shouldThrowStatusConflictWhenPatientAlreadyHasClinicalRecord() {
        Long patientId = 1L;
        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest("reason", "diagnosis", "obs", "medication");
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setClinicalRecord(new ClinicalRecord());
        when(patientRepo.findById(patientId)).thenReturn(Optional.of(patient));
        var ex = assertThrows(StatusConflictException.class, () -> clinicalRecordService.createClinicalRecord(patientId, request));
        assertEquals("Patient with id " + patientId + " already has a clinical record.", ex.getMessage());
        verify(patientRepo).findById(patientId);
    }

    @Test
    void shouldUpdateClinicalRecordSuccessfully() {
        Long id = 1L;
        ClinicalRecord clinicalRecord = createClinicalRecord(id, "reason", "diagnosis", "obs", "medication");
        Patient patient = new Patient();
            patient.setFirstname("Juan");
            patient.setLastname("Pérez");
        clinicalRecord.setPatient(patient);
        LocalDate date = LocalDate.now();
        Session session = createSession(1L, date, "evolution notes");
        clinicalRecord.setSessions(List.of(session));

        when(clinicalRecordRepo.findById(id)).thenReturn(Optional.of(clinicalRecord));

        ClinicalRecordUpdateRequest updateRequest = new ClinicalRecordUpdateRequest("newReason", "newDiagnosis", "newObs", "newMedication");
        when(clinicalRecordRepo.save(any(ClinicalRecord.class))).thenReturn(clinicalRecord);

        // Act
        ClinicalRecordResponse response = clinicalRecordService.updateClinicalRecord(id, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals("Juan", response.firstname());
        assertEquals("Pérez", response.lastname());
        assertEquals("newDiagnosis", response.diagnosis());
        assertEquals("newObs", response.obs());
        assertEquals("newMedication", response.medication());
        assertNotNull(response.sessions());
        assertEquals(1L, response.sessions().size());
        assertEquals(1L, response.sessions().get(0).sessionId());
        assertEquals(date, response.sessions().get(0).sessionDate());
        assertEquals("evolution notes", response.sessions().get(0).evolutionNotes());

        verify(clinicalRecordRepo).findById(id);
        verify(clinicalRecordRepo).save(clinicalRecord);
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        Long id = 1L;

        ClinicalRecord clinicalRecord = createClinicalRecord(id, "reason", "diagnosis", "obs", "medication");

        Patient patient = new Patient();
        patient.setFirstname("Juan");
        patient.setLastname("Pérez");
        clinicalRecord.setPatient(patient);

        when(clinicalRecordRepo.findById(id)).thenReturn(Optional.of(clinicalRecord));
        when(clinicalRecordRepo.save(any(ClinicalRecord.class))).thenReturn(clinicalRecord);

        ClinicalRecordUpdateRequest request = new ClinicalRecordUpdateRequest(
                null, "newDiagnosis", null, null
        );

        ClinicalRecordResponse response = clinicalRecordService.updateClinicalRecord(id, request);

        assertEquals("reason", clinicalRecord.getReasonConsult());
        assertEquals("newDiagnosis", clinicalRecord.getDiagnosis());
        assertEquals("obs", clinicalRecord.getObs());
        assertEquals("medication", clinicalRecord.getMedication());

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals("Juan", response.firstname());
        assertEquals("Pérez", response.lastname());
        assertEquals("newDiagnosis", response.diagnosis());
        assertEquals("obs", response.obs());
        assertEquals("medication", response.medication());
    }

    @Test
    void shouldThrowResourceNotFoundWhenUpdatingNonexistentClinicalRecord() {
        Long id = 99L;
        ClinicalRecordUpdateRequest updateRequest = new ClinicalRecordUpdateRequest("reason", "diagnosis", "obs", "medication");
        when(clinicalRecordRepo.findById(id)).thenReturn(Optional.empty());
        var ex = assertThrows(ResourceNotFoundException.class, () -> clinicalRecordService.updateClinicalRecord(id, updateRequest));
        assertEquals("ClinicalRecord not found with id: " + id, ex.getMessage());
        verify(clinicalRecordRepo).findById(id);
    }

    // Helpers
    private ClinicalRecord createClinicalRecord(Long id, String reasonConsult, String diagnosis, String obs, String medication) {
        ClinicalRecord cr = new ClinicalRecord();
        cr.setId(id);
        cr.setReasonConsult(reasonConsult);
        cr.setDiagnosis(diagnosis);
        cr.setObs(obs);
        cr.setMedication(medication);
        return cr;
    }

    private Session createSession(Long id, LocalDate sessionDate, String evolutionNotes) {
        Session session = new Session();
        session.setId(id);
        session.setSessionDate(sessionDate);
        session.setEvolutionNotes(evolutionNotes);
        return session;
    }

    
    
}
