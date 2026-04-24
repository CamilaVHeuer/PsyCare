package com.camicompany.PsyCare.Integration;

import com.camicompany.PsyCare.dto.sessionDTO.SessionCreateRequest;
import com.camicompany.PsyCare.dto.sessionDTO.SessionUpdateRequest;
import com.camicompany.PsyCare.model.ClinicalRecord;
import com.camicompany.PsyCare.model.Patient;
import com.camicompany.PsyCare.model.PatientStatus;
import com.camicompany.PsyCare.model.Session;
import com.camicompany.PsyCare.repository.ClinicalRecordRepository;
import com.camicompany.PsyCare.repository.PatientRepository;
import com.camicompany.PsyCare.repository.SessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class SessionIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private ClinicalRecordRepository clinicalRecordRepository;
    @Autowired private PatientRepository patientRepository;

    private static final String SESSIONS_URL = "/api/v1/sessions";
    private static final String CLINICAL_RECORDS_URL = "/api/v1/clinical-records";

    private ClinicalRecord savedClinicalRecord;
    private Session savedSession;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        clinicalRecordRepository.deleteAll();
        patientRepository.deleteAll();

        // 1. Create and save patient
        Patient patient = new Patient();
        patient.setFirstname("Manuel");
        patient.setLastname("Suarez");
        patient.setNationalId("12345678");
        patient.setPhone("123456789");
        patient.setStatus(PatientStatus.ACTIVE);

        // 2. Create clinical record and set up bidirectional relationship
        ClinicalRecord clinicalRecord = new ClinicalRecord();
        clinicalRecord.setDiagnosis("Chronic anxiety");
        clinicalRecord.setObs("Difficulty sleeping");
        clinicalRecord.setMedication("Anxiolytics");
        clinicalRecord.setPatient(patient);
        patient.setClinicalRecord(clinicalRecord);

        // 3. Save patient (owner of the FK to clinical_record)
        Patient savedPatient = patientRepository.save(patient);
        savedClinicalRecord = savedPatient.getClinicalRecord();

        // 4. Create and save session
        Session session = new Session();
        session.setSessionDate(LocalDate.of(2026, 4, 10));
        session.setEvolutionNotes("First follow-up session");
        session.setClinicalRecord(savedClinicalRecord);
        savedSession = sessionRepository.save(session);
    }

    // ── GET /sessions/{id} ───────────────────────────────────────────────────

    @Test
    void getSessionByIdShouldReturn200WhenExists() throws Exception {
        mockMvc.perform(get(SESSIONS_URL + "/" + savedSession.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").value(savedSession.getId()))
                .andExpect(jsonPath("$.sessionDate").value("2026-04-10"))
                .andExpect(jsonPath("$.evolutionNotes").value("First follow-up session"));
    }

    @Test
    void getSessionByIdShouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get(SESSIONS_URL + "/99999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Session not found with id 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── POST /clinical-records/{id}/sessions ─────────────────────────────────

    @Test
    void createSessionShouldReturn201WithValidRequest() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest(
                LocalDate.of(2026, 4, 15),
                "Second session: patient reports improvement"
        );

        mockMvc.perform(post(CLINICAL_RECORDS_URL + "/" + savedClinicalRecord.getId() + "/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", containsString("/api/v1/sessions/")))
                .andExpect(jsonPath("$.sessionId").exists())
                .andExpect(jsonPath("$.sessionDate").value("2026-04-15"))
                .andExpect(jsonPath("$.evolutionNotes").value("Second session: patient reports improvement"));

        assertEquals(2, sessionRepository.count());
    }

    @Test
    void createSessionShouldReturn404WhenClinicalRecordNotFound() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest(
                LocalDate.of(2026, 4, 15),
                "Session notes"
        );

        mockMvc.perform(post(CLINICAL_RECORDS_URL + "/99999/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Clinical record not found with id 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createSessionShouldReturn400WhenDateIsNull() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest(null, "Session notes");

        mockMvc.perform(post(CLINICAL_RECORDS_URL + "/" + savedClinicalRecord.getId() + "/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The date cannot be empty")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createSessionShouldReturn400WhenDateIsInTheFuture() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest(
                LocalDate.now().plusDays(5),
                "Session notes"
        );

        mockMvc.perform(post(CLINICAL_RECORDS_URL + "/" + savedClinicalRecord.getId() + "/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The date cannot be in the future")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createSessionShouldReturn400WhenNotesIsBlank() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest(
                LocalDate.of(2026, 4, 15),
                ""
        );

        mockMvc.perform(post(CLINICAL_RECORDS_URL + "/" + savedClinicalRecord.getId() + "/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("You must enter at least one note for the record")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PATCH /sessions/{id} ─────────────────────────────────────────────────

    @Test
    void updateSessionShouldReturn200WithAllFields() throws Exception {
        SessionUpdateRequest request = new SessionUpdateRequest(
                LocalDate.of(2026, 4, 20),
                "Updated notes: patient shows progress"
        );

        mockMvc.perform(patch(SESSIONS_URL + "/" + savedSession.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").value(savedSession.getId()))
                .andExpect(jsonPath("$.sessionDate").value("2026-04-20"))
                .andExpect(jsonPath("$.evolutionNotes").value("Updated notes: patient shows progress"));
    }

    @Test
    void updateSessionShouldReturn200WhenOnlyNotesProvided() throws Exception {
        SessionUpdateRequest request = new SessionUpdateRequest(null, "Only notes updated");

        mockMvc.perform(patch(SESSIONS_URL + "/" + savedSession.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").value(savedSession.getId()))
                .andExpect(jsonPath("$.sessionDate").value("2026-04-10"))   // date unchanged
                .andExpect(jsonPath("$.evolutionNotes").value("Only notes updated"));
    }

    @Test
    void updateSessionShouldReturn200WhenOnlyDateProvided() throws Exception {
        SessionUpdateRequest request = new SessionUpdateRequest(LocalDate.of(2026, 4, 22), null);

        mockMvc.perform(patch(SESSIONS_URL + "/" + savedSession.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").value(savedSession.getId()))
                .andExpect(jsonPath("$.sessionDate").value("2026-04-22"))
                .andExpect(jsonPath("$.evolutionNotes").value("First follow-up session")); // notes unchanged
    }

    @Test
    void updateSessionShouldReturn404WhenNotFound() throws Exception {
        SessionUpdateRequest request = new SessionUpdateRequest(LocalDate.of(2026, 4, 20), "Notes");

        mockMvc.perform(patch(SESSIONS_URL + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Session not found with id 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateSessionShouldReturn400WhenDateIsInTheFuture() throws Exception {
        SessionUpdateRequest request = new SessionUpdateRequest(LocalDate.now().plusDays(3), "Notes");

        mockMvc.perform(patch(SESSIONS_URL + "/" + savedSession.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The date cannot be in the future")))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
