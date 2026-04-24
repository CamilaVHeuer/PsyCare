package com.camicompany.PsyCare.Integration;

import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceCreateRequest;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceUpdateRequest;
import com.camicompany.PsyCare.model.Insurance;
import com.camicompany.PsyCare.repository.InsuranceRepository;
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
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class InsuranceIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private InsuranceRepository insuranceRepository;

    private static final String BASE_URL = "/api/v1/insurances";

    private Insurance savedInsurance;

    @BeforeEach
    void setUp() {
        insuranceRepository.deleteAll();

        Insurance insurance = new Insurance();
        insurance.setName("OSDE");
        insurance.setCuit("30123456789");
        savedInsurance = insuranceRepository.save(insurance);

        Insurance insurance2 = new Insurance();
        insurance2.setName("SWISS MEDICAL");
        insurance2.setCuit("30987654321");
        insuranceRepository.save(insurance2);
    }

    // ── GET / ────────────────────────────────────────────────────────────────

    @Test
    void getAllInsurancesShouldReturn200WithList() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(savedInsurance.getId()))
                .andExpect(jsonPath("$[0].name").value("OSDE"))
                .andExpect(jsonPath("$[0].cuit").value("30-12345678-9"))
                .andExpect(jsonPath("$[1].name").value("SWISS MEDICAL"));
    }

    @Test
    void getAllInsurancesShouldReturn200WithEmptyList() throws Exception {
        insuranceRepository.deleteAll();

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /{id} ────────────────────────────────────────────────────────────

    @Test
    void getInsuranceByIdShouldReturn200WhenExists() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + savedInsurance.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedInsurance.getId()))
                .andExpect(jsonPath("$.name").value("OSDE"))
                .andExpect(jsonPath("$.cuit").value("30-12345678-9"));
    }

    @Test
    void getInsuranceByIdShouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/99999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Insurance not found with id 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── GET /name/{name} ─────────────────────────────────────────────────────

    @Test
    void getInsuranceByNameShouldReturn200WhenExists() throws Exception {
        mockMvc.perform(get(BASE_URL + "/name/OSDE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedInsurance.getId()))
                .andExpect(jsonPath("$.name").value("OSDE"))
                .andExpect(jsonPath("$.cuit").value("30-12345678-9"));
    }

    @Test
    void getInsuranceByNameShouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/name/NOEXIST"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Insurance not found with name NOEXIST"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── POST / ───────────────────────────────────────────────────────────────

    @Test
    void createInsuranceShouldReturn201WithValidRequest() throws Exception {
        InsuranceCreateRequest request = new InsuranceCreateRequest("MEDIFE", "30-11223344-5");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", containsString("/api/v1/insurances/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("MEDIFE"))
                .andExpect(jsonPath("$.cuit").value("30-11223344-5"));
    }

    @Test
    void createInsuranceShouldReturn409WhenNameAlreadyExists() throws Exception {
        InsuranceCreateRequest request = new InsuranceCreateRequest("OSDE", "30-11223344-5");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("OSDE")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createInsuranceShouldReturn409WhenCuitAlreadyExists() throws Exception {
        InsuranceCreateRequest request = new InsuranceCreateRequest("MEDIFE", "30-12345678-9");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("already exists")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createInsuranceShouldReturn400WhenNameIsBlank() throws Exception {
        InsuranceCreateRequest request = new InsuranceCreateRequest("", "30-11223344-5");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The name of the health insurance is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createInsuranceShouldReturn400WhenCuitIsInvalid() throws Exception {
        InsuranceCreateRequest request = new InsuranceCreateRequest("MEDIFE", "invalid--cuit");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The CUIT must have the format XX-XXXXXXXX-X")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PATCH /{id} ──────────────────────────────────────────────────────────

    @Test
    void updateInsuranceShouldReturn200WithAllFields() throws Exception {
        InsuranceUpdateRequest request = new InsuranceUpdateRequest("MEDIFE", "30-11223344-5");

        mockMvc.perform(patch(BASE_URL + "/" + savedInsurance.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedInsurance.getId()))
                .andExpect(jsonPath("$.name").value("MEDIFE"))
                .andExpect(jsonPath("$.cuit").value("30-11223344-5"));
    }

    @Test
    void updateInsuranceShouldReturn200WithOnlyName() throws Exception {
        InsuranceUpdateRequest request = new InsuranceUpdateRequest("MEDIFE", null);

        mockMvc.perform(patch(BASE_URL + "/" + savedInsurance.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedInsurance.getId()))
                .andExpect(jsonPath("$.name").value("MEDIFE"))
                .andExpect(jsonPath("$.cuit").value("30-12345678-9"));
    }

    @Test
    void updateInsuranceShouldReturn409WhenNameAlreadyBelongsToAnother() throws Exception {
        InsuranceUpdateRequest request = new InsuranceUpdateRequest("SWISS MEDICAL", null);

        mockMvc.perform(patch(BASE_URL + "/" + savedInsurance.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("SWISS MEDICAL")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateInsuranceShouldReturn409WhenCuitAlreadyBelongsToAnother() throws Exception {
        InsuranceUpdateRequest request = new InsuranceUpdateRequest(null, "30-98765432-1");

        mockMvc.perform(patch(BASE_URL + "/" + savedInsurance.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("already exists")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateInsuranceShouldReturn404WhenNotFound() throws Exception {
        InsuranceUpdateRequest request = new InsuranceUpdateRequest("MEDIFE", null);

        mockMvc.perform(patch(BASE_URL + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Insurance not found with id 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateInsuranceShouldReturn400WhenCuitIsInvalid() throws Exception {
        InsuranceUpdateRequest request = new InsuranceUpdateRequest(null, "invalid--cuit");

        mockMvc.perform(patch(BASE_URL + "/" + savedInsurance.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The CUIT must have the format XX-XXXXXXXX-X")))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
