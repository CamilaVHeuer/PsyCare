package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.sessionDTO.SessionResponse;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordCreateRequest;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordResponse;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordUpdateRequest;
import com.camicompany.PsyCare.exception.*;
import com.camicompany.PsyCare.security.config.filter.JwtTokenValidator;
import com.camicompany.PsyCare.service.ClinicalRecordService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ClinicalRecordController.class, excludeAutoConfiguration =  SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class ClinicalRecordControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ClinicalRecordService clinicalRecordServ;
    @MockitoBean private JwtTokenValidator jwtTokenValidator;
    @Autowired private ObjectMapper objectMapper;

    private final static String BASE_URL = "/api/v1";

    @Test
    void getClinicalRecordShouldReturn200() throws Exception {
        LocalDate date = LocalDate.of(2024, 6, 1);
        SessionResponse sessionResponse = new SessionResponse(1L, date, "evolution notes");
        ClinicalRecordResponse response = new ClinicalRecordResponse(1L, "Juan", "Pérez", "diagnosis", "obs", "medication", List.of(sessionResponse));
        when(clinicalRecordServ.getClinicalRecordById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL+ "/clinical-records/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstname").value("Juan"))
                .andExpect(jsonPath("$.lastname").value("Pérez"))
                .andExpect(jsonPath("$.diagnosis").value("diagnosis"))
                .andExpect(jsonPath("$.obs").value("obs"))
                .andExpect(jsonPath("$.medication").value("medication"))
                .andExpect(jsonPath("$.sessions[0].sessionId").value(1L))
                .andExpect(jsonPath("$.sessions[0].sessionDate").value("2024-06-01"))
                .andExpect(jsonPath("$.sessions[0].evolutionNotes").value("evolution notes"));
        verify(clinicalRecordServ).getClinicalRecordById(1L);
    }
    @Test
    void getClinicalRecordWithoutSessionShouldReturn200() throws Exception {
        ClinicalRecordResponse response = new ClinicalRecordResponse(1L, "Juan", "Pérez", "diagnosis", "obs", "medication", new ArrayList<>());
        when(clinicalRecordServ.getClinicalRecordById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL+ "/clinical-records/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstname").value("Juan"))
                .andExpect(jsonPath("$.lastname").value("Pérez"))
                .andExpect(jsonPath("$.diagnosis").value("diagnosis"))
                .andExpect(jsonPath("$.obs").value("obs"))
                .andExpect(jsonPath("$.medication").value("medication"))
                .andExpect(jsonPath("$.sessions").isArray())
                .andExpect(jsonPath("$.sessions.length()").value(0));

        verify(clinicalRecordServ).getClinicalRecordById(1L);
    }

    @Test
    void getClinicalRecordShouldReturn404WhenNotFound() throws Exception {
        when(clinicalRecordServ.getClinicalRecordById(99L)).thenThrow(new ResourceNotFoundException("ClinicalRecord not found with id: 99"));

        mockMvc.perform(get(BASE_URL + "/clinical-records/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("ClinicalRecord not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createClinicalRecordShouldReturn201() throws Exception {
        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest("reason", "diagnosis", "obs", "medication");
        ClinicalRecordResponse response = new ClinicalRecordResponse(1L, "Juan", "Pérez", "diagnosis", "obs", "medication", new ArrayList<>());
        when(clinicalRecordServ.createClinicalRecord(eq(1L), any(ClinicalRecordCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/patients/1/clinical-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/clinical-records/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.diagnosis").value("diagnosis"))
                .andExpect(jsonPath("$.obs").value("obs"))
                .andExpect(jsonPath("$.medication").value("medication"))
                .andExpect(jsonPath("$.sessions").isArray())
                .andExpect(jsonPath("$.sessions.length()").value(0));
            verify(clinicalRecordServ).createClinicalRecord(eq(1L), any(ClinicalRecordCreateRequest.class));
    }

    @Test
    void createClinicalRecordShouldReturn404WhenPatientNotFound() throws Exception {
        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest("reason", "diagnosis", "obs", "medication");
        when(clinicalRecordServ.createClinicalRecord(eq(99L), any(ClinicalRecordCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Patient not found with id: 99"));

        mockMvc.perform(post(BASE_URL + "/patients/99/clinical-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createClinicalRecordShouldReturn409WhenPatientAlreadyHasClinicalRecord() throws Exception {
        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest("reason", "diagnosis", "obs", "medication");
        when(clinicalRecordServ.createClinicalRecord(eq(1L), any(ClinicalRecordCreateRequest.class)))
                .thenThrow(new StatusConflictException("Patient with id 1 already has a clinical record."));

        mockMvc.perform(post( BASE_URL + "/patients/1/clinical-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Patient with id 1 already has a clinical record."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateClinicalRecordShouldReturn200() throws Exception {
        ClinicalRecordUpdateRequest updateRequest = new ClinicalRecordUpdateRequest("reason", "diagnosis", "obs", "medication");
        LocalDate date = LocalDate.of(2024, 6, 1);
        SessionResponse sessionResponse = new SessionResponse(1L, date, "evolution notes");
        ClinicalRecordResponse response = new ClinicalRecordResponse(1L, "Juan", "Pérez", "diagnosis", "obs", "medication", List.of(sessionResponse));
        when(clinicalRecordServ.updateClinicalRecord(eq(1L), any(ClinicalRecordUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/clinical-records/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.diagnosis").value("diagnosis"))
                .andExpect(jsonPath("$.obs").value("obs"))
                .andExpect(jsonPath("$.medication").value("medication"))
                .andExpect(jsonPath("$.sessions[0].sessionId").value(1L))
                .andExpect(jsonPath("$.sessions[0].sessionDate").value("2024-06-01"))
                .andExpect(jsonPath("$.sessions[0].evolutionNotes").value("evolution notes"));
        verify(clinicalRecordServ).updateClinicalRecord(eq(1L), any(ClinicalRecordUpdateRequest.class));
    }

    @Test
    void updateClinicalRecordShouldReturn200WhenUpdateOnlyOneField() throws Exception {
        ClinicalRecordUpdateRequest updateRequest = new ClinicalRecordUpdateRequest(null, null, null, "medication");
        LocalDate date = LocalDate.of(2024, 6, 1);
        SessionResponse sessionResponse = new SessionResponse(1L, date, "evolution notes");
        ClinicalRecordResponse response = new ClinicalRecordResponse(1L, "Juan", "Pérez", "diagnosis", "obs", "medication", List.of(sessionResponse));
        when(clinicalRecordServ.updateClinicalRecord(eq(1L), any(ClinicalRecordUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/clinical-records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.diagnosis").value("diagnosis"))
                .andExpect(jsonPath("$.obs").value("obs"))
                .andExpect(jsonPath("$.medication").value("medication"))
                .andExpect(jsonPath("$.sessions[0].sessionId").value(1L))
                .andExpect(jsonPath("$.sessions[0].sessionDate").value("2024-06-01"))
                .andExpect(jsonPath("$.sessions[0].evolutionNotes").value("evolution notes"));
        verify(clinicalRecordServ).updateClinicalRecord(eq(1L), any(ClinicalRecordUpdateRequest.class));
    }

    @Test
    void updateClinicalRecordShouldReturn404WhenNotFound() throws Exception {
        ClinicalRecordUpdateRequest updateRequest = new ClinicalRecordUpdateRequest("reason", "diagnosis", "obs", "medication");
        when(clinicalRecordServ.updateClinicalRecord(eq(99L), any(ClinicalRecordUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("ClinicalRecord not found with id: 99"));

        mockMvc.perform(patch(BASE_URL + "/clinical-records/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("ClinicalRecord not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createClinicalRecordShouldReturn400WhenReasonConsultIsBlank() throws Exception {
        ClinicalRecordCreateRequest request = new ClinicalRecordCreateRequest(
                "",
                "diagnosis",
                "obs",
                "medication"
        );

        mockMvc.perform(post(BASE_URL + "/patients/1/clinical-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The reason for consultation is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }


}
