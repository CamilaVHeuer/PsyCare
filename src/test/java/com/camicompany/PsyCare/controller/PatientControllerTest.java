package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.patientDTO.PatientCreateRequest;
import com.camicompany.PsyCare.dto.patientDTO.PatientResponse;
import com.camicompany.PsyCare.dto.patientDTO.PatientSummaryResponse;
import com.camicompany.PsyCare.dto.patientDTO.PatientUpdateRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorResponse;
import com.camicompany.PsyCare.exception.ConflictingTutorInformationException;
import com.camicompany.PsyCare.exception.DuplicateNationalIdException;
import com.camicompany.PsyCare.exception.MissingTutorException;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;

import com.camicompany.PsyCare.model.PatientStatus;
import com.camicompany.PsyCare.model.TutorRelation;
import com.camicompany.PsyCare.security.config.filter.JwtTokenValidator;
import com.camicompany.PsyCare.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@WebMvcTest(controllers = PatientController.class, excludeAutoConfiguration =  SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class PatientControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private PatientService patientServ;
    @MockitoBean private JwtTokenValidator jwtTokenValidator;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/patients";

    @Test
    void getPatientByIdShouldReturn200() throws Exception {
        PatientResponse response = new PatientResponse(
                1L, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), Period.between(LocalDate.of(2000, 1, 1), LocalDate.now()).getYears(), "12345678", PatientStatus.ACTIVE, null, null, null
        );
        when(patientServ.getPatientById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.firstname").value("Juan"))
                .andExpect(jsonPath("$.lastname").value("Pérez"))
                .andExpect(jsonPath("$.nationalId").value("12345678"))
                .andExpect(jsonPath("$.birthDate").value("2000-01-01"))
                .andExpect(jsonPath("$.age").value(Period.between(LocalDate.of(2000, 1, 1), LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.phone").value("12345678"))
                .andExpect(jsonPath("$.status").value(PatientStatus.ACTIVE.name()));

        verify(patientServ).getPatientById(1L);
    }

   @Test
    void getAllPatientsShouldReturn200() throws Exception {
       PatientSummaryResponse summary1 = new PatientSummaryResponse(
               1L, "Juan", "Pérez", Period.between(LocalDate.of(2000, 1, 1), LocalDate.now()).getYears(), null);
       PatientSummaryResponse summary2 = new PatientSummaryResponse(
               2L, "Juana", "González", Period.between(LocalDate.of(2000, 1, 15), LocalDate.now()).getYears(), "IPS");
       when(patientServ.getAllPatients()).thenReturn(List.of(summary1, summary2));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$[0].patientId").value(1L))
                .andExpect(jsonPath("$[0].firstname").value("Juan"))
                .andExpect(jsonPath("$[0].lastname").value("Pérez"))
                .andExpect(jsonPath("$[0].age").value(Period.between(LocalDate.of(2000, 1, 1), LocalDate.now()).getYears()))
                .andExpect(jsonPath("$[0].insuranceName").doesNotExist())
                .andExpect(jsonPath("$[1].patientId").value(2L))
                .andExpect(jsonPath("$[1].firstname").value("Juana"))
                .andExpect(jsonPath("$[1].lastname").value("González"))
                .andExpect(jsonPath("$[1].age").value(Period.between(LocalDate.of(2000, 1, 15), LocalDate.now()).getYears()))
                .andExpect(jsonPath("$[1].insuranceName").value("IPS"))
                .andExpect(jsonPath("$.length()").value(2));

            verify(patientServ).getAllPatients();
    }
    @Test
    void getPatientByIdShouldReturn404() throws Exception {
        when(patientServ.getPatientById(99L)).thenThrow(new ResourceNotFoundException("Patient not found with id: 99"));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getAllPatientsShouldReturnEmptyList() throws Exception {
        when(patientServ.getAllPatients()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

   @Test
    void createAdultPatientShouldReturn201() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678", null, null, null, null
        );
        PatientResponse response = new PatientResponse(
                1L, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), Period.between(LocalDate.of(2000, 1, 1), LocalDate.now()).getYears(), "12345678", PatientStatus.ACTIVE, null, null, null
        );
        when(patientServ.createPatient(any(PatientCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BASE_URL + "/1"))
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.firstname").value("Juan"))
                .andExpect(jsonPath("$.lastname").value("Pérez"))
                .andExpect(jsonPath("$.nationalId").value("12345678"))
                .andExpect(jsonPath("$.birthDate").value("2000-01-01"))
                .andExpect(jsonPath("$.age").value(Period.between(LocalDate.of(2000, 1, 1), LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.phone").value("12345678"))
                .andExpect(jsonPath("$.status").value(PatientStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.tutor").doesNotExist())
                .andExpect(jsonPath("$.insuranceName").doesNotExist())
                .andExpect(jsonPath("$.insuranceNumber").doesNotExist());
        verify(patientServ).createPatient(request);
    }
    @Test
    void createMinorPatientWithTutorIdShouldReturn201() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Ana", "Gomez", "87654321", LocalDate.of(2015, 1, 1), null, 2L, null, null, null
        );

        TutorResponse tutorResponse = new TutorResponse(2L, "Pedro",
                "Gomez", "12345678", "20-12345678-1", TutorRelation.FATHER);
        PatientResponse response = new PatientResponse(
                1L, "Ana", "Gomez", "87654321", LocalDate.of(2015, 1, 1),
                Period.between(LocalDate.of(2015, 1, 1), LocalDate.now()).getYears(),
                "12345678", PatientStatus.ACTIVE, tutorResponse,
                null, null
        );

        when(patientServ.createPatient(any(PatientCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BASE_URL + "/1"))
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.firstname").value("Ana"))
                .andExpect(jsonPath("$.lastname").value("Gomez"))
                .andExpect(jsonPath("$.nationalId").value("87654321"))
                .andExpect(jsonPath("$.birthDate").value("2015-01-01"))
                .andExpect(jsonPath("$.age").value(Period.between(LocalDate.of(2015, 1, 1), LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.status").value(PatientStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.tutor.id").value(2L))
                .andExpect(jsonPath("$.tutor.firstname").value("Pedro"))
                .andExpect(jsonPath("$.tutor.lastname").value("Gomez"))
                .andExpect(jsonPath("$.tutor.phone").value("12345678"))
                .andExpect(jsonPath("$.tutor.cuil").value("20-12345678-1"))
                .andExpect(jsonPath("$.tutor.relation").value("FATHER"));
        verify(patientServ).createPatient(request);
    }
    @Test
    void createMinorPatientWithTutorObjectShouldReturn201() throws Exception {
       TutorCreateRequest tutorRequest = new TutorCreateRequest(
                        "Pedro", "Gomez", "12345678", "20-12345678-1", TutorRelation.FATHER
                );

        PatientCreateRequest request = new PatientCreateRequest(
                "Ana", "Gomez", "87654321", LocalDate.of(2015, 1, 1), null, null, tutorRequest, 1L, "1234"
        );
        TutorResponse tutorResponse = new TutorResponse(2L, "Pedro",
                "Gomez", "12345678", "20-12345678-1", TutorRelation.FATHER);
        PatientResponse response = new PatientResponse(
                1L, "Ana", "Gomez", "87654321", LocalDate.of(2015, 1, 1),
                Period.between(LocalDate.of(2015, 1, 1), LocalDate.now()).getYears(),
                "12345678", PatientStatus.ACTIVE,
               tutorResponse, "IPS", "1234");

        when(patientServ.createPatient(any(PatientCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BASE_URL + "/1"))
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.firstname").value("Ana"))
                .andExpect(jsonPath("$.lastname").value("Gomez"))
                .andExpect(jsonPath("$.nationalId").value("87654321"))
                .andExpect(jsonPath("$.birthDate").value("2015-01-01"))
                .andExpect(jsonPath("$.age").value(Period.between(LocalDate.of(2015, 1, 1), LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.status").value(PatientStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.insuranceName").value("IPS"))
                .andExpect(jsonPath("$.insuranceNumber").value("1234"))
                .andExpect(jsonPath("$.tutor.id").value(2L))
                .andExpect(jsonPath("$.tutor.firstname").value("Pedro"))
                .andExpect(jsonPath("$.tutor.lastname").value("Gomez"))
                .andExpect(jsonPath("$.tutor.phone").value("12345678"))
                .andExpect(jsonPath("$.tutor.cuil").value("20-12345678-1"))
                .andExpect(jsonPath("$.tutor.relation").value("FATHER"));
        verify(patientServ).createPatient(request);
    }
    @Test
    void createPatientShouldReturn400WhenFirstnameIsBlank() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678", null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The first name cannot be empty")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createPatientShouldReturn400WhenLastnameIsBlank() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Juan", "", "12345678", LocalDate.of(2000, 1, 1), "12345678", null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The last name cannot be empty")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createPatientShouldReturn400WhenNationalIdIsBlank() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Juan", "Pérez", "", LocalDate.of(2000, 1, 1), "12345678", null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createPatientShouldReturn400WhenNationalIdIsInvalid() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Juan", "Pérez", "abc", LocalDate.of(2000, 1, 1), "12345678", null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid DNI")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createPatientShouldReturn400WhenBirthDateIsNull() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Juan", "Pérez", "12345678", null, "12345678", null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The birth date is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createPatientShouldReturn400WhenBirthDateIsInFuture() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Juan", "Pérez", "12345678", LocalDate.now().plusDays(1), "12345678", null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The birth date cannot be in the future")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createPatientShouldReturn400WhenTutorIsInvalid() throws Exception {
        // Tutor con nombre vacío y cuil inválido
        TutorCreateRequest tutorRequest = new TutorCreateRequest(
                "", "Gomez", "12345678", "123", TutorRelation.FATHER
        );
        PatientCreateRequest request = new PatientCreateRequest(
                "Ana", "Gomez", "87654321", LocalDate.of(2015, 1, 1), "12345678", null, tutorRequest, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createPatientShouldReturn400WhenPhoneIsTooLong() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "123456789012345678901", null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void createMinorPatientWithoutTutorShouldReturn400() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Ana", "Gomez", "87654321", LocalDate.of(2020, 1, 1), "12345678", null, null, null, null
        );

        when(patientServ.createPatient(any(PatientCreateRequest.class)))
                .thenThrow(new MissingTutorException("Minor patients require tutor information"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Minor patients require tutor information"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void createMinorPatientWithNonexistentTutorIdShouldReturn404() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Ana", "Gomez", "87654321", LocalDate.of(2015, 1, 1), "12345678", 99L, null, null, null
        );
        when(patientServ.createPatient(any(PatientCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Tutor not found with id: 99"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Tutor not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createMinorPatientWithTutorIdAndTutorShouldReturn409() throws Exception {
        TutorCreateRequest tutorRequest = new TutorCreateRequest(
                "Pedro", "Gomez", "12345678", "20-12345678-1", TutorRelation.FATHER
        );
        PatientCreateRequest request = new PatientCreateRequest(
                "Ana", "Gomez", "87654321", LocalDate.of(2015, 1, 1), "12345678", 2L, tutorRequest, null, null
        );
        when(patientServ.createPatient(any(PatientCreateRequest.class)))
                .thenThrow(new ConflictingTutorInformationException("Provide either tutorId or tutor info, not both"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Provide either tutorId or tutor info, not both"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void createAdultPatientWithInvalidInsuranceShouldReturn404() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678", null, null, 99L, "9999"
        );
        when(patientServ.createPatient(any(PatientCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Insurance not found with id: 99"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Insurance not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void createAdultPatientWithDuplicateNationalIdShouldReturn409() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678", null, null, null, null
        );
        when(patientServ.createPatient(any(PatientCreateRequest.class)))
                .thenThrow(new DuplicateNationalIdException("Patient with this national ID already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Patient with this national ID already exists"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void updateAdultPatientBasicFieldsSuccessfully() throws Exception {
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                "Juana", "Pérez", "87654321", LocalDate.of(2000, 1, 1), "87654321", null, null, null, null
        );
        PatientResponse response = new PatientResponse(
                1L, "Juana", "Pérez", "87654321", LocalDate.of(2000, 1, 1),
                Period.between(LocalDate.of(2000, 1, 1), LocalDate.now()).getYears(),
                "87654321", PatientStatus.ACTIVE, null, null, null
        );
        when(patientServ.updatePatient(eq(1L), any(PatientUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.firstname").value("Juana"))
                .andExpect(jsonPath("$.lastname").value("Pérez"))
                .andExpect(jsonPath("$.nationalId").value("87654321"))
                .andExpect(jsonPath("$.birthDate").value("2000-01-01"))
                .andExpect(jsonPath("$.phone").value("87654321"))
                .andExpect(jsonPath("$.age").value(Period.between(LocalDate.of(2000, 1, 1), LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.status").value(PatientStatus.ACTIVE.name()));

        verify(patientServ).updatePatient(eq(1L), any(PatientUpdateRequest.class));
    }

    @Test
    void updatePatientShouldReturn404WhenPatientNotFound() throws Exception {
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                "Juan", "Pérez", "12345678", null, null, null, null, null, null
        );

        when(patientServ.updatePatient(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Patient not found with id: 99"));

        mockMvc.perform(patch(BASE_URL + "/99")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePatientWithInvalidInsuranceShouldReturn404() throws Exception {
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                null, null, null, null, null, null, null, 99L, "9999"
        );
        when(patientServ.updatePatient(eq(1L), any(PatientUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Insurance not found with id: 99"));

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Insurance not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void updatePatientWithDuplicateNationalIdShouldReturn409() throws Exception {
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                null, null, "87654321", null, null, null, null, null, null
        );
        when(patientServ.updatePatient(eq(1L), any(PatientUpdateRequest.class)))
                .thenThrow(new DuplicateNationalIdException("Patient with this national ID already exists"));

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Patient with this national ID already exists"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void updatePatientWithTutorIdAndTutorShouldReturn409() throws Exception {
        TutorCreateRequest tutorRequest = new TutorCreateRequest(
                "Pedro", "Gomez", "12345678", "20-12345678-1", TutorRelation.FATHER
        );
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                null, null, null, LocalDate.of(2015, 1, 1), null, 2L, tutorRequest, null, null
        );
        when(patientServ.updatePatient(eq(1L), any(PatientUpdateRequest.class)))
                .thenThrow(new ConflictingTutorInformationException("Provide either tutorId or tutor info, not both"));

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Provide either tutorId or tutor info, not both"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void updateMinorPatientWithoutTutorShouldReturn400() throws Exception {
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                null, null, null, LocalDate.of(2020, 1, 1), null, null, null, null, null
        );
        when(patientServ.updatePatient(eq(1L), any(PatientUpdateRequest.class)))
                .thenThrow(new MissingTutorException("Minor patients require tutor information"));

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Minor patients require tutor information"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void updateMinorPatientWithNonexistentTutorIdShouldReturn404() throws Exception {
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                null, null, null, LocalDate.of(2015, 1, 1), null, 99L, null, null, null
        );
        when(patientServ.updatePatient(eq(1L), any(PatientUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Tutor not found with id: 99"));

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Tutor not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void updateMinorPatientTutorSuccessfully() throws Exception {
        TutorResponse newTutor = new TutorResponse(3L, "Ana", "Gómez", "87654321", "20-87654321-2", TutorRelation.MOTHER);
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                null, null, null, LocalDate.of(2015, 1, 1), null, 3L, null, null, null
        );
        PatientResponse response = new PatientResponse(
                1L, "Ana", "Gomez", "87654321", LocalDate.of(2015, 1, 1),
                Period.between(LocalDate.of(2015, 1, 1), LocalDate.now()).getYears(),
                "87654321", PatientStatus.ACTIVE, newTutor, null, null
        );
        when(patientServ.updatePatient(eq(1L), any(PatientUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.tutor.id").value(3L))
                .andExpect(jsonPath("$.tutor.firstname").value("Ana"))
                .andExpect(jsonPath("$.tutor.lastname").value("Gómez"))
                .andExpect(jsonPath("$.tutor.cuil").value("20-87654321-2"))
                .andExpect(jsonPath("$.tutor.relation").value("MOTHER"));
    }
    @Test
    void updateMinorPatientInsuranceSuccessfully() throws Exception {
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                null, null, null, LocalDate.of(2015, 1, 1), null, null, null, 5L, "9999"
        );
        TutorResponse tutor = new TutorResponse(2L, "Pedro", "Gomez", "12345678", "20-12345678-1", TutorRelation.FATHER);
        PatientResponse response = new PatientResponse(
                1L, "Ana", "Gomez", "87654321", LocalDate.of(2015, 1, 1),
                Period.between(LocalDate.of(2015, 1, 1), LocalDate.now()).getYears(),
                "12345678", PatientStatus.ACTIVE, tutor, "OSDE", "9999"
        );
        when(patientServ.updatePatient(eq(1L), any(PatientUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.insuranceName").value("OSDE"))
                .andExpect(jsonPath("$.insuranceNumber").value("9999"));
    }
    @Test
    void updateAllFieldsSuccessfully() throws Exception {
        TutorResponse tutor = new TutorResponse(4L, "Carlos", "Lopez", "11112222", "20-11112222-3", TutorRelation.LEGAL_GUARDIAN);
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                "Carla", "Lopez", "55555555", LocalDate.of(2010, 5, 5), "99999999", 4L, null, 7L, "8888"
        );
        PatientResponse response = new PatientResponse(
                1L, "Carla", "Lopez", "55555555", LocalDate.of(2010, 5, 5),
                Period.between(LocalDate.of(2010, 5, 5), LocalDate.now()).getYears(),
                "99999999", PatientStatus.ACTIVE, tutor, "SWISS MEDICAL", "8888"
        );
        when(patientServ.updatePatient(eq(1L), any(PatientUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.firstname").value("Carla"))
                .andExpect(jsonPath("$.lastname").value("Lopez"))
                .andExpect(jsonPath("$.nationalId").value("55555555"))
                .andExpect(jsonPath("$.birthDate").value("2010-05-05"))
                .andExpect(jsonPath("$.age").value(Period.between(LocalDate.of(2010, 5, 5), LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.phone").value("99999999"))
                .andExpect(jsonPath("$.tutor.id").value(4L))
                .andExpect(jsonPath("$.tutor.firstname").value("Carlos"))
                .andExpect(jsonPath("$.insuranceName").value("SWISS MEDICAL"))
                .andExpect(jsonPath("$.insuranceNumber").value("8888"));
    }

    @Test
    void dischargePatientShouldReturn200() throws Exception {
        PatientResponse response = new PatientResponse(
                1L, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1),
                Period.between(LocalDate.of(2000, 1, 1), LocalDate.now()).getYears(),
                "12345678", PatientStatus.DISCHARGED, null, null, null
        );
        when(patientServ.dischargePatient(1L)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/discharge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.firstname").value("Juan"))
                .andExpect(jsonPath("$.lastname").value("Pérez"))
                .andExpect(jsonPath("$.nationalId").value("12345678"))
                .andExpect(jsonPath("$.birthDate").value("2000-01-01"))
                .andExpect(jsonPath("$.age").value(Period.between(LocalDate.of(2000, 1, 1), LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.phone").value("12345678"))
                .andExpect(jsonPath("$.status").value(PatientStatus.DISCHARGED.name()))
                .andExpect(jsonPath("$.tutor").doesNotExist())
                .andExpect(jsonPath("$.insuranceName").doesNotExist())
                .andExpect(jsonPath("$.insuranceNumber").doesNotExist());
        verify(patientServ).dischargePatient(1L);
    }

    @Test
    void reactivePatientShouldReturn200() throws Exception {
        PatientResponse response = new PatientResponse(
                1L, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1),
                Period.between(LocalDate.of(2000, 1, 1), LocalDate.now()).getYears(),
                "12345678", PatientStatus.ACTIVE, null, null, null
        );
        when(patientServ.reactivePatient(1L)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/reactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.firstname").value("Juan"))
                .andExpect(jsonPath("$.lastname").value("Pérez"))
                .andExpect(jsonPath("$.nationalId").value("12345678"))
                .andExpect(jsonPath("$.birthDate").value("2000-01-01"))
                .andExpect(jsonPath("$.age").value(Period.between(LocalDate.of(2000, 1, 1), LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.phone").value("12345678"))
                .andExpect(jsonPath("$.status").value(PatientStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.tutor").doesNotExist())
                .andExpect(jsonPath("$.insuranceName").doesNotExist())
                .andExpect(jsonPath("$.insuranceNumber").doesNotExist());
        verify(patientServ).reactivePatient(1L);
    }
    @Test
    void dischargePatientShouldReturn409WhenAlreadyDischarged() throws Exception {
        when(patientServ.dischargePatient(1L))
                .thenThrow(new com.camicompany.PsyCare.exception.StatusConflictException("Patient is already discharged"));

        mockMvc.perform(put(BASE_URL + "/1/discharge"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Patient is already discharged"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void dischargePatientShouldReturn404WhenPatientNotFound() throws Exception {
        when(patientServ.dischargePatient(99L))
                .thenThrow(new ResourceNotFoundException("Patient not found with id: 99"));

        mockMvc.perform(put(BASE_URL + "/99/discharge"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void reactivePatientShouldReturn409WhenAlreadyActive() throws Exception {
        when(patientServ.reactivePatient(1L))
                .thenThrow(new com.camicompany.PsyCare.exception.StatusConflictException("Patient is already active"));

        mockMvc.perform(put(BASE_URL + "/1/reactive"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Patient is already active"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void reactivePatientShouldReturn404WhenPatientNotFound() throws Exception {
        when(patientServ.reactivePatient(99L))
                .thenThrow(new ResourceNotFoundException("Patient not found with id: 99"));

        mockMvc.perform(put(BASE_URL + "/99/reactive"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());

    }

    @Test
    void updatePatientShouldReturn400WhenNationalIdIsInvalid() throws Exception {
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                "Juan", "Pérez", "abc", null, null, null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePatientShouldReturn400WhenBirthDateIsInFuture() throws Exception {
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                "Juan", "Pérez", "12345678", LocalDate.now().plusDays(1), null, null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePatientShouldReturn400WhenPhoneIsTooLong() throws Exception {
        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "123456789012345678901", null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
