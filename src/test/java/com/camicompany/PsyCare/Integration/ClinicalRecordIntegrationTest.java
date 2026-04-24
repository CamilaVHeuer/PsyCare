package com.camicompany.PsyCare.Integration;

import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordCreateRequest;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordUpdateRequest;
import com.camicompany.PsyCare.model.ClinicalRecord;
import com.camicompany.PsyCare.model.Patient;
import com.camicompany.PsyCare.model.PatientStatus;
import com.camicompany.PsyCare.repository.ClinicalRecordRepository;
import com.camicompany.PsyCare.repository.PatientRepository;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class ClinicalRecordIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PatientRepository patientRepository;
    @Autowired private ClinicalRecordRepository clinicalRecordRepository;

    private static final String BASE_URL = "/api/v1";

    private Patient savedPatient;
    private Patient patientWithRecord;
    private ClinicalRecord savedRecord;

    @BeforeEach
    void setUp() {
        clinicalRecordRepository.deleteAll();
        patientRepository.deleteAll();

        // Patient without clinical record (to test creation)
        Patient patient = new Patient();
        patient.setFirstname("Juan");
        patient.setLastname("Pérez");
        patient.setNationalId("12345678");
        patient.setPhone("123456789");
        patient.setStatus(PatientStatus.ACTIVE);
        savedPatient = patientRepository.save(patient);

        // Patient with clinical record already (to test duplicate conflict)
        Patient patientWithRec = new Patient();
        patientWithRec.setFirstname("Ana");
        patientWithRec.setLastname("Lopez");
        patientWithRec.setNationalId("87654321");
        patientWithRec.setPhone("987654321");
        patientWithRec.setStatus(PatientStatus.ACTIVE);

        ClinicalRecord record = new ClinicalRecord();
        record.setReasonConsult("Anxiety");
        record.setDiagnosis("Chronic anxiety");
        record.setObs("Does not sleep well");
        record.setMedication("Anxiolytics");

        patientWithRec.setClinicalRecord(record);
        record.setPatient(patientWithRec);

        patientWithRecord = patientRepository.save(patientWithRec);
        savedRecord = patientWithRecord.getClinicalRecord();
    }

    // ── GET /clinical-records/{id} ───────────────────────────────────────────

    @Test
    void getClinicalRecordByIdShouldReturn200WhenExists() throws Exception {
        mockMvc.perform(get(BASE_URL + "/clinical-records/" + savedRecord.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedRecord.getId()))
                .andExpect(jsonPath("$.firstname").value("Ana"))
                .andExpect(jsonPath("$.lastname").value("Lopez"))
                .andExpect(jsonPath("$.diagnosis").value("Chronic anxiety"))
                .andExpect(jsonPath("$.obs").value("Does not sleep well"))
                .andExpect(jsonPath("$.medication").value("Anxiolytics"))
                .andExpect(jsonPath("$.sessions").isArray())
                .andExpect(jsonPath("$.sessions.length()").value(0));
    }

    @Test
    void getClinicalRecordByIdShouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/clinical-records/99999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("ClinicalRecord not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── POST /patients/{patientId}/clinical-record ───────────────────────────

    @Test
    void createClinicalRecordShouldReturn201WithValidRequest() throws Exception {
        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest(
                "Stress", "Acute stress", "Work overload", "Therapy"
        );

        mockMvc.perform(post(BASE_URL + "/patients/" + savedPatient.getId() + "/clinical-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", containsString("/api/v1/clinical-records/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstname").value("Juan"))
                .andExpect(jsonPath("$.lastname").value("Pérez"))
                .andExpect(jsonPath("$.diagnosis").value("Acute stress"))
                .andExpect(jsonPath("$.obs").value("Work overload"))
                .andExpect(jsonPath("$.medication").value("Therapy"))
                .andExpect(jsonPath("$.sessions").isArray())
                .andExpect(jsonPath("$.sessions.length()").value(0));
    }

    @Test
    void createClinicalRecordShouldReturn404WhenPatientNotFound() throws Exception {
        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest(
                "Stress", "Acute stress", "Work overload", "Therapy"
        );

        mockMvc.perform(post(BASE_URL + "/patients/99999/clinical-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createClinicalRecordShouldReturn409WhenPatientAlreadyHasRecord() throws Exception {
        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest(
                "Stress", "Acute stress", "Work overload", "Therapy"
        );

        mockMvc.perform(post(BASE_URL + "/patients/" + patientWithRecord.getId() + "/clinical-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("already has a clinical record")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createClinicalRecordShouldReturn400WhenReasonConsultIsBlank() throws Exception {
        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest(
                "", "Acute stress", "Work overload", "Therapy"
        );

        mockMvc.perform(post(BASE_URL + "/patients/" + savedPatient.getId() + "/clinical-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The reason for consultation is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PATCH /clinical-records/{id} ─────────────────────────────────────────

    @Test
    void updateClinicalRecordShouldReturn200WithAllFields() throws Exception {
        ClinicalRecordUpdateRequest request = new ClinicalRecordUpdateRequest(
                "Updated reason", "Updated diagnosis", "Updated obs", "Updated medication"
        );

        mockMvc.perform(patch(BASE_URL + "/clinical-records/" + savedRecord.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedRecord.getId()))
                .andExpect(jsonPath("$.firstname").value("Ana"))
                .andExpect(jsonPath("$.lastname").value("Lopez"))
                .andExpect(jsonPath("$.diagnosis").value("Updated diagnosis"))
                .andExpect(jsonPath("$.obs").value("Updated obs"))
                .andExpect(jsonPath("$.medication").value("Updated medication"))
                .andExpect(jsonPath("$.sessions").isArray());
    }

    @Test
    void updateClinicalRecordShouldReturn200WhenOnlyOneFieldProvided() throws Exception {
        ClinicalRecordUpdateRequest request = new ClinicalRecordUpdateRequest(
                null, null, null, "New medication"
        );

        mockMvc.perform(patch(BASE_URL + "/clinical-records/" + savedRecord.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedRecord.getId()))
                .andExpect(jsonPath("$.diagnosis").value("Chronic anxiety"))
                .andExpect(jsonPath("$.obs").value("Does not sleep well"))
                .andExpect(jsonPath("$.medication").value("New medication"));
    }

    @Test
    void updateClinicalRecordShouldReturn404WhenNotFound() throws Exception {
        ClinicalRecordUpdateRequest request = new ClinicalRecordUpdateRequest(
                null, "New diagnosis", null, null
        );

        mockMvc.perform(patch(BASE_URL + "/clinical-records/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("ClinicalRecord not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
