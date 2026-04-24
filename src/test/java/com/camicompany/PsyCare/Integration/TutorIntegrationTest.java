package com.camicompany.PsyCare.Integration;

import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRelationRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRequest;
import com.camicompany.PsyCare.model.Tutor;
import com.camicompany.PsyCare.model.TutorRelation;
import com.camicompany.PsyCare.repository.TutorRepository;
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
public class TutorIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TutorRepository tutorRepository;

    private static final String BASE_URL = "/api/v1/tutors";

    private Tutor savedTutor;

    @BeforeEach
    void setUp() {
        tutorRepository.deleteAll();

        Tutor tutor = new Tutor();
        tutor.setFirstname("Pedro");
        tutor.setLastname("Pérez");
        tutor.setPhone("12345678");
        tutor.setCuil("20123456781"); // stored without dashes
        tutor.setRelation(TutorRelation.FATHER);
        savedTutor = tutorRepository.save(tutor);

        // second tutor — persisted for CUIL conflict tests
        Tutor tutor2 = new Tutor();
        tutor2.setFirstname("Ana");
        tutor2.setLastname("Gomez");
        tutor2.setPhone("87654321");
        tutor2.setCuil("27876543212"); // stored without dashes
        tutor2.setRelation(TutorRelation.MOTHER);
        tutorRepository.save(tutor2);
    }

    // ── POST / ───────────────────────────────────────────────────────────────

    @Test
    void createTutorShouldReturn201WithValidRequest() throws Exception {
        TutorCreateRequest request = new TutorCreateRequest(
                "Carlos", "Lopez", "11112222", "20-11112222-3", TutorRelation.LEGAL_GUARDIAN
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", containsString("/api/v1/tutors/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstname").value("Carlos"))
                .andExpect(jsonPath("$.lastname").value("Lopez"))
                .andExpect(jsonPath("$.phone").value("11112222"))
                .andExpect(jsonPath("$.cuil").value("20-11112222-3"))
                .andExpect(jsonPath("$.relation").value("LEGAL_GUARDIAN"));
    }

    @Test
    void createTutorShouldReturn409WhenCuilAlreadyExists() throws Exception {
        TutorCreateRequest request = new TutorCreateRequest(
                "Carlos", "Lopez", "11112222", "20-12345678-1", TutorRelation.FATHER
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("The CUIL " + request.cuil() + " is already registered for another tutor")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createTutorShouldReturn400WhenFirstnameIsBlank() throws Exception {
        TutorCreateRequest request = new TutorCreateRequest(
                "", "Lopez", "11112222", "20-11112222-3", TutorRelation.FATHER
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The tutor's first name is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createTutorShouldReturn400WhenLastnameIsBlank() throws Exception {
        TutorCreateRequest request = new TutorCreateRequest(
                "Carlos", "", "11112222", "20-11112222-3", TutorRelation.FATHER
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The tutor's last name is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createTutorShouldReturn400WhenCuilIsInvalid() throws Exception {
        TutorCreateRequest request = new TutorCreateRequest(
                "Carlos", "Lopez", "11112222", "no-cuilFormat", TutorRelation.FATHER
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("CUIL must have the format XX-XXXXXXXX-X")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createTutorShouldReturn400WhenPhoneIsBlank() throws Exception {
        TutorCreateRequest request = new TutorCreateRequest(
                "Carlos", "Lopez", "", "20-11112222-3", TutorRelation.FATHER
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The tutor's phone number is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createTutorShouldReturn400WhenRelationIsNull() throws Exception {
        TutorCreateRequest request = new TutorCreateRequest(
                "Carlos", "Lopez", "11112222", "20-11112222-3", null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("Relation is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PATCH /{id} ──────────────────────────────────────────────────────────

    @Test
    void updateTutorShouldReturn200WithAllFields() throws Exception {
        TutorUpdateRequest request = new TutorUpdateRequest(
                "PedroUpdated", "PerezUpdated", "99999999", "20-12345678-1"
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedTutor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedTutor.getId()))
                .andExpect(jsonPath("$.firstname").value("PedroUpdated"))
                .andExpect(jsonPath("$.lastname").value("PerezUpdated"))
                .andExpect(jsonPath("$.phone").value("99999999"))
                .andExpect(jsonPath("$.cuil").value("20-12345678-1"))
                .andExpect(jsonPath("$.relation").value("FATHER"));
    }

    @Test
    void updateTutorShouldReturn200WithOnlyOneField() throws Exception {
        TutorUpdateRequest request = new TutorUpdateRequest(
                "PedroUpdated", null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedTutor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedTutor.getId()))
                .andExpect(jsonPath("$.firstname").value("PedroUpdated"))
                .andExpect(jsonPath("$.lastname").value("Pérez"))
                .andExpect(jsonPath("$.phone").value("12345678"))
                .andExpect(jsonPath("$.cuil").value("20-12345678-1"))
                .andExpect(jsonPath("$.relation").value("FATHER"));
    }

    @Test
    void updateTutorShouldReturn409WhenCuilAlreadyBelongsToAnother() throws Exception {
        TutorUpdateRequest request = new TutorUpdateRequest(
                null, null, null, "27-87654321-2"
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedTutor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("The CUIL " +request.cuil() + " is already registered for another tutor")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateTutorShouldReturn404WhenNotFound() throws Exception {
        TutorUpdateRequest request = new TutorUpdateRequest(
                "Carlos", null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Tutor not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateTutorShouldReturn400WhenCuilIsInvalid() throws Exception {
        TutorUpdateRequest request = new TutorUpdateRequest(
                null, null, null, "no-cuilFormat"
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedTutor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("CUIL must have the format XX-XXXXXXXX-X")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PUT /{id}/relation ───────────────────────────────────────────────────

    @Test
    void changeRelationShouldReturn200AndUpdateRelation() throws Exception {
        TutorUpdateRelationRequest request = new TutorUpdateRelationRequest(TutorRelation.LEGAL_GUARDIAN);

        mockMvc.perform(put(BASE_URL + "/" + savedTutor.getId() + "/relation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedTutor.getId()))
                .andExpect(jsonPath("$.firstname").value("Pedro"))
                .andExpect(jsonPath("$.lastname").value("Pérez"))
                .andExpect(jsonPath("$.cuil").value("20-12345678-1"))
                .andExpect(jsonPath("$.relation").value("LEGAL_GUARDIAN"));
    }

    @Test
    void changeRelationShouldReturn404WhenNotFound() throws Exception {
        TutorUpdateRelationRequest request = new TutorUpdateRelationRequest(TutorRelation.FATHER);

        mockMvc.perform(put(BASE_URL + "/99999/relation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Tutor not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void changeRelationShouldReturn400WhenRelationIsNull() throws Exception {
        TutorUpdateRelationRequest request = new TutorUpdateRelationRequest(null);

        mockMvc.perform(put(BASE_URL + "/" + savedTutor.getId() + "/relation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("Relation is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
