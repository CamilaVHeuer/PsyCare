package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorResponse;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRelationRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.exception.StatusConflictException;
import com.camicompany.PsyCare.model.TutorRelation;
import com.camicompany.PsyCare.security.config.filter.JwtTokenValidator;
import com.camicompany.PsyCare.service.TutorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TutorController.class, excludeAutoConfiguration =  SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class TutorControllerTest {

	@Autowired private MockMvc mockMvc;
	@MockitoBean private TutorService tutorServ;
	@MockitoBean private JwtTokenValidator jwtTokenValidator;
	@Autowired private ObjectMapper objectMapper;

	private static final String BASE_URL = "/api/v1/tutors";

	@Test
	void createTutorShouldReturn201() throws Exception {
		TutorCreateRequest request = new TutorCreateRequest("Pedro", "Pérez", "12345678", "20-12345678-1", TutorRelation.FATHER);
		TutorResponse response = new TutorResponse(1L, "Pedro", "Pérez", "12345678", "20-12345678-1", TutorRelation.FATHER);
		when(tutorServ.createTutor(any(TutorCreateRequest.class))).thenReturn(response);

		mockMvc.perform(post(BASE_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", BASE_URL + "/1"))
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.firstname").value("Pedro"))
				.andExpect(jsonPath("$.lastname").value("Pérez"))
				.andExpect(jsonPath("$.phone").value("12345678"))
				.andExpect(jsonPath("$.cuil").value("20-12345678-1"))
				.andExpect(jsonPath("$.relation").value(TutorRelation.FATHER.name()));
		verify(tutorServ).createTutor(request);
	}
	@Test
	void createTutorShouldReturn400WhenFirstnameIsBlank() throws Exception {
		TutorCreateRequest request = new TutorCreateRequest("", "Pérez", "12345678", "20-12345678-1", TutorRelation.FATHER);
		mockMvc.perform(post(BASE_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value(containsString("The tutor's first name is required")))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void createTutorShouldReturn400WhenLastnameIsBlank() throws Exception {
		TutorCreateRequest request = new TutorCreateRequest("Pedro", "", "12345678", "20-12345678-1", TutorRelation.FATHER);
		mockMvc.perform(post(BASE_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value(containsString("The tutor's last name is required")))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void createTutorShouldReturn400WhenPhoneIsTooLong() throws Exception {
		TutorCreateRequest request = new TutorCreateRequest("Pedro", "Pérez", "123456789012345678901", "20-12345678-1", TutorRelation.FATHER);
		mockMvc.perform(post(BASE_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").exists())
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void createTutorShouldReturn400WhenCuilIsInvalid() throws Exception {
		TutorCreateRequest request = new TutorCreateRequest("Pedro", "Pérez", "12345678", "no-cuilFormat", TutorRelation.FATHER);
		mockMvc.perform(post(BASE_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value(containsString("CUIL must have the format XX-XXXXXXXX-X")))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void createTutorShouldReturn409WhenCuilAlreadyExists() throws Exception {
		TutorCreateRequest request = new TutorCreateRequest(
				"Pedro", "Pérez", "12345678", "20-12345678-1", TutorRelation.FATHER
		);
		when(tutorServ.createTutor(any()))
				.thenThrow(new StatusConflictException("The CUIL is already registered for another tutor"));

		mockMvc.perform(post(BASE_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.error").value("CONFLICT"))
				.andExpect(jsonPath("$.message").value("The CUIL is already registered for another tutor"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void updateTutorShouldReturn200() throws Exception {
		TutorUpdateRequest updateRequest = new TutorUpdateRequest("Pedro", "Pérez", "87654321", "20-12345678-1");
		TutorResponse response = new TutorResponse(1L, "Pedro", "Pérez", "87654321", "20-12345678-1", TutorRelation.FATHER);
		when(tutorServ.updateTutor(eq(1L), any(TutorUpdateRequest.class))).thenReturn(response);

		mockMvc.perform(patch(BASE_URL + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.firstname").value("Pedro"))
				.andExpect(jsonPath("$.lastname").value("Pérez"))
				.andExpect(jsonPath("$.phone").value("87654321"))
				.andExpect(jsonPath("$.cuil").value("20-12345678-1"))
				.andExpect(jsonPath("$.relation").value(TutorRelation.FATHER.name()));
		verify(tutorServ).updateTutor(eq(1L), any(TutorUpdateRequest.class));
	}

	@Test
	void updateTutorWithAllNullFieldsShouldStillReturn200() throws Exception {
		TutorUpdateRequest updateRequest = new TutorUpdateRequest(null, null, null, null);
		TutorResponse response = new TutorResponse(
				1L, "Pedro", "Pérez", "12345678", "20-12345678-1", TutorRelation.FATHER
		);
		when(tutorServ.updateTutor(eq(1L), any())).thenReturn(response);

		mockMvc.perform(patch(BASE_URL + "/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").value(1L))
		.andExpect(jsonPath("$.firstname").value("Pedro"))
		.andExpect(jsonPath("$.lastname").value("Pérez"))
		.andExpect(jsonPath("$.phone").value("12345678"))
		.andExpect(jsonPath("$.cuil").value("20-12345678-1"))
		.andExpect(jsonPath("$.relation").value(TutorRelation.FATHER.name()));
		verify(tutorServ).updateTutor(eq(1L), any(TutorUpdateRequest.class));
	}

	@Test
	void updateTutorShouldReturn404WhenNotFound() throws Exception {
		TutorUpdateRequest updateRequest = new TutorUpdateRequest("Pedro", "Pérez", "87654321", "20-12345678-1");
		when(tutorServ.updateTutor(eq(99L), any(TutorUpdateRequest.class)))
				.thenThrow(new ResourceNotFoundException("Tutor not found with id: 99"));

		mockMvc.perform(patch(BASE_URL + "/99")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Tutor not found with id: 99"))
				.andExpect(jsonPath("$.timestamp").exists());
	}


	@Test
	void updateTutorShouldReturn400WhenPhoneIsTooLong() throws Exception {
		TutorUpdateRequest updateRequest = new TutorUpdateRequest("Pedro", "Pérez", "123456789012345678901", "20-12345678-1");
		mockMvc.perform(patch(BASE_URL + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").exists())
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void updateTutorShouldReturn400WhenCuilIsInvalid() throws Exception {
		TutorUpdateRequest updateRequest = new TutorUpdateRequest("Pedro", "Pérez", "12345678", "no-cuilFormat");
		mockMvc.perform(patch(BASE_URL + "/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value(containsString("CUIL must have the format XX-XXXXXXXX-X")))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void changeRelationTutorPatientShouldReturn200() throws Exception {
		TutorUpdateRelationRequest relationRequest = new TutorUpdateRelationRequest(TutorRelation.FATHER);
		TutorResponse response = new TutorResponse(1L, "Pedro", "Pérez", "12345678", "20-12345678-1", TutorRelation.FATHER);
		when(tutorServ.changeRelationTutorPatient(eq(1L), any(TutorUpdateRelationRequest.class))).thenReturn(response);

		mockMvc.perform(put(BASE_URL + "/1/relation")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(relationRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.lastname").value("Pérez"))
				.andExpect(jsonPath("$.phone").value("12345678"))
				.andExpect(jsonPath("$.cuil").value("20-12345678-1"))
				.andExpect(jsonPath("$.relation").value(TutorRelation.FATHER.name()));
		verify(tutorServ).changeRelationTutorPatient(eq(1L), any(TutorUpdateRelationRequest.class));
	}

    @Test
    void changeRelationTutorPatientShouldReturn404WhenNotFound() throws Exception {
        TutorUpdateRelationRequest relationRequest = new TutorUpdateRelationRequest(TutorRelation.FATHER);
        when(tutorServ.changeRelationTutorPatient(eq(99L), any(TutorUpdateRelationRequest.class)))
                .thenThrow(new com.camicompany.PsyCare.exception.ResourceNotFoundException("Tutor not found with id: 99"));

        mockMvc.perform(put(BASE_URL + "/99/relation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Tutor not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

	@Test
	void changeRelationShouldReturn400WhenRelationIsNull() throws Exception {
		String json = """
        {
            "relation": null
        }
        """;

		mockMvc.perform(put(BASE_URL + "/1/relation")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status").value(400))
		.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
		.andExpect(jsonPath("$.message").value(containsString("Relation is required")))
		.andExpect(jsonPath("$.timestamp").exists());
	}
}
