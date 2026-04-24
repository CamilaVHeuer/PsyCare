package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceCreateRequest;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceResponse;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceUpdateRequest;
import com.camicompany.PsyCare.security.config.filter.JwtTokenValidator;
import com.camicompany.PsyCare.service.InsuranceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.camicompany.PsyCare.exception.*;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;


@WebMvcTest(controllers = InsuranceController.class, excludeAutoConfiguration =  SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class InsuranceControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private InsuranceService insuranceService;
    @MockitoBean private JwtTokenValidator jwtTokenValidator;

    private final static String BASE_URL = "/api/v1/insurances";

    @Test
    void getAllInsurancesShouldReturn200() throws Exception {
        InsuranceResponse resp1 = new InsuranceResponse(1L, "OSDE", "30-12345678-9");
        InsuranceResponse resp2 = new InsuranceResponse(2L, "SWISS MEDICAL", "30-98765432-1");
        when(insuranceService.getAllInsurances()).thenReturn(List.of(resp1, resp2));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("OSDE"))
                .andExpect(jsonPath("$[1].name").value("SWISS MEDICAL"));
        verify(insuranceService).getAllInsurances();
    }

    @Test
    void getInsuranceByIdShouldReturn200() throws Exception {
        InsuranceResponse resp = new InsuranceResponse(1L, "OSDE", "30-12345678-9");
        when(insuranceService.getInsuranceById(1L)).thenReturn(resp);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("OSDE"))
                .andExpect(jsonPath("$.cuit").value("30-12345678-9"));

        verify(insuranceService).getInsuranceById(1L);
    }

    @Test
    void getInsuranceByIdShouldReturn404() throws Exception {
        when(insuranceService.getInsuranceById(99L)).thenThrow(new ResourceNotFoundException("Insurance not found with id: 99"));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Insurance not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getInsuranceByNameShouldReturn200() throws Exception {
        InsuranceResponse resp = new InsuranceResponse(1L, "OSDE", "30-12345678-9");
        when(insuranceService.getInsuranceByName("OSDE")).thenReturn(resp);

        mockMvc.perform(get(BASE_URL + "/name/OSDE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("OSDE"));
    }

    @Test
    void getInsuranceByNameShouldReturn404() throws Exception {
        when(insuranceService.getInsuranceByName("OSDE"))
                .thenThrow(new ResourceNotFoundException("Insurance not found"));

        mockMvc.perform(get(BASE_URL + "/name/OSDE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Insurance not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createInsuranceShouldReturn201() throws Exception {
        InsuranceCreateRequest request = new InsuranceCreateRequest("OSDE", "30-12345678-9");
        InsuranceResponse response = new InsuranceResponse(1L, "OSDE", "30-12345678-9");
        when(insuranceService.createInsurance(any(InsuranceCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BASE_URL + "/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("OSDE"))
                .andExpect(jsonPath("$.cuit").value("30-12345678-9"));
    }

    @Test
    void createInsuranceShouldReturn409WhenAlreadyExists() throws Exception {
        InsuranceCreateRequest request = new InsuranceCreateRequest("OSDE", "30-12345678-9");

        when(insuranceService.createInsurance(any()))
                .thenThrow(new StatusConflictException("Insurance already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Insurance already exists"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createInsuranceShouldReturn400WhenNameIsBlank() throws Exception {
        InsuranceCreateRequest request = new InsuranceCreateRequest("", "30-12345678-9");
        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The name of the health insurance is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createInsuranceShouldReturn400WhenCuitIsInvalid() throws Exception {
        InsuranceCreateRequest request = new InsuranceCreateRequest("OSDE", "invalid--cuit");
        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The CUIT must have the format XX-XXXXXXXX-X")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateInsuranceShouldReturn200() throws Exception {
        InsuranceUpdateRequest updateRequest = new InsuranceUpdateRequest("SWISS MEDICAL", "30-98765432-1");
        InsuranceResponse response = new InsuranceResponse(1L, "SWISS MEDICAL", "30-98765432-1");
        when(insuranceService.updateInsurance(eq(1L), any(InsuranceUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("SWISS MEDICAL"))
                .andExpect(jsonPath("$.cuit").value("30-98765432-1"));
        verify(insuranceService).updateInsurance(1L, updateRequest);
    }
    @Test
    void updateInsuranceShouldAllowNullName() throws Exception {
        InsuranceUpdateRequest request = new InsuranceUpdateRequest(null, "30-12345678-1");

        when(insuranceService.updateInsurance(eq(1L), any()))
                .thenReturn(new InsuranceResponse(1L, "OSDE", "30-12345678-1"));

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("OSDE"))
                .andExpect(jsonPath("$.cuit").value("30-12345678-1"));
    }

    @Test
    void updateInsuranceShouldReturn404() throws Exception {
        InsuranceUpdateRequest updateRequest = new InsuranceUpdateRequest("SWISS MEDICAL", "30-98765432-1");
        when(insuranceService.updateInsurance(eq(99L), any(InsuranceUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Insurance not found with id: 99"));

        mockMvc.perform(patch(BASE_URL + "/99")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Insurance not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateInsuranceShouldReturn400WhenCuitIsInvalid() throws Exception {
        InsuranceUpdateRequest updateRequest = new InsuranceUpdateRequest("SWISS MEDICAL", "invalid--cuit");
        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The CUIT must have the format XX-XXXXXXXX-X")))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
