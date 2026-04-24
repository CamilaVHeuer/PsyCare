package com.camicompany.PsyCare.Integration;

import com.camicompany.PsyCare.dto.patientDTO.PatientCreateRequest;
import com.camicompany.PsyCare.dto.patientDTO.PatientUpdateRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.model.*;
import com.camicompany.PsyCare.repository.InsuranceRepository;
import com.camicompany.PsyCare.repository.PatientRepository;
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

import java.time.LocalDate;
import java.time.Period;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class PatientIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PatientRepository patientRepository;
    @Autowired private TutorRepository tutorRepository;
    @Autowired private InsuranceRepository insuranceRepository;

    private static final String BASE_URL = "/api/v1/patients";

    private static final LocalDate ADULT_BIRTH_DATE   = LocalDate.of(1990, 5, 20);
    private static final LocalDate MINOR_BIRTH_DATE   = LocalDate.now().minusYears(10);

    private Patient savedPatient;
    private Patient savedDischargedPatient;
    private Tutor   savedTutor;
    private Insurance savedInsurance;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
        tutorRepository.deleteAll();
        insuranceRepository.deleteAll();

        Tutor tutor = new Tutor();
        tutor.setFirstname("Pedro");
        tutor.setLastname("Pérez");
        tutor.setPhone("12345678");
        tutor.setCuil("20123456781");
        tutor.setRelation(TutorRelation.FATHER);
        savedTutor = tutorRepository.save(tutor);

        Insurance insurance = new Insurance();
        insurance.setName("IPS");
        insurance.setCuit("30123456781");
        savedInsurance = insuranceRepository.save(insurance);

        Patient patient = new Patient();
        patient.setFirstname("Manuel");
        patient.setLastname("Suarez");
        patient.setNationalId("12345678");
        patient.setBirthDate(ADULT_BIRTH_DATE);
        patient.setPhone("11112222");
        patient.setStatus(PatientStatus.ACTIVE);
        savedPatient = patientRepository.save(patient);

        Patient discharged = new Patient();
        discharged.setFirstname("Ana");
        discharged.setLastname("Lopez");
        discharged.setNationalId("87654321");
        discharged.setBirthDate(ADULT_BIRTH_DATE);
        discharged.setPhone("33334444");
        discharged.setStatus(PatientStatus.DISCHARGED);
        savedDischargedPatient = patientRepository.save(discharged);
    }

    // ── GET /{id} ────────────────────────────────────────────────────────────

    @Test
    void getPatientByIdShouldReturn200WhenExists() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + savedPatient.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.patientId").value(savedPatient.getId()))
                .andExpect(jsonPath("$.firstname").value("Manuel"))
                .andExpect(jsonPath("$.lastname").value("Suarez"))
                .andExpect(jsonPath("$.nationalId").value("12345678"))
                .andExpect(jsonPath("$.birthDate").value(ADULT_BIRTH_DATE.toString()))
                .andExpect(jsonPath("$.age").value(Period.between(ADULT_BIRTH_DATE, LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.phone").value("11112222"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getPatientByIdShouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/99999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── GET / ────────────────────────────────────────────────────────────────

    @Test
    void getAllPatientsShouldReturn200WithList() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].patientId").exists())
                .andExpect(jsonPath("$[0].firstname").exists());
    }

    @Test
    void getAllPatientsShouldReturn200WithEmptyList() throws Exception {
        patientRepository.deleteAll();

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── POST / ───────────────────────────────────────────────────────────────

    @Test
    void createAdultPatientShouldReturn201() throws Exception {
        LocalDate birthDate = LocalDate.of(1985, 3, 15);
        PatientCreateRequest request = new PatientCreateRequest(
                "Carlos", "Gomez", "11223344",
                birthDate, "55556666",
                null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", containsString("/api/v1/patients/")))
                .andExpect(jsonPath("$.patientId").exists())
                .andExpect(jsonPath("$.firstname").value("Carlos"))
                .andExpect(jsonPath("$.lastname").value("Gomez"))
                .andExpect(jsonPath("$.nationalId").value("11223344"))
                .andExpect(jsonPath("$.birthDate").value(birthDate.toString()))
                .andExpect(jsonPath("$.age").value(Period.between(birthDate, LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.phone").value("55556666"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.tutor").doesNotExist())
                .andExpect(jsonPath("$.insuranceName").doesNotExist())
                .andExpect(jsonPath("$.insuranceNumber").doesNotExist());
    }

    @Test
    void createMinorPatientWithTutorIdShouldReturn201() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Lucía", "Perez", "55443322",
                MINOR_BIRTH_DATE, null,
                savedTutor.getId(), null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", containsString("/api/v1/patients/")))
                .andExpect(jsonPath("$.patientId").exists())
                .andExpect(jsonPath("$.firstname").value("Lucía"))
                .andExpect(jsonPath("$.lastname").value("Perez"))
                .andExpect(jsonPath("$.nationalId").value("55443322"))
                .andExpect(jsonPath("$.birthDate").value(MINOR_BIRTH_DATE.toString()))
                .andExpect(jsonPath("$.age").value(Period.between(MINOR_BIRTH_DATE, LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.tutor.id").value(savedTutor.getId()))
                .andExpect(jsonPath("$.tutor.firstname").value("Pedro"))
                .andExpect(jsonPath("$.tutor.lastname").value("Pérez"))
                .andExpect(jsonPath("$.tutor.cuil").value("20-12345678-1"))
                .andExpect(jsonPath("$.tutor.relation").value("FATHER"))
                .andExpect(jsonPath("$.insuranceName").doesNotExist())
                .andExpect(jsonPath("$.insuranceNumber").doesNotExist());
    }

    @Test
    void createMinorPatientWithTutorObjectShouldReturn201() throws Exception {
        TutorCreateRequest tutorReq = new TutorCreateRequest(
                "Rosa", "Diaz", "99887766", "27-99887766-4", TutorRelation.MOTHER
        );
        PatientCreateRequest request = new PatientCreateRequest(
                "Tomás", "Diaz", "66778899",
                MINOR_BIRTH_DATE, null,
                null, tutorReq, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", containsString("/api/v1/patients/")))
                .andExpect(jsonPath("$.patientId").exists())
                .andExpect(jsonPath("$.firstname").value("Tomás"))
                .andExpect(jsonPath("$.lastname").value("Diaz"))
                .andExpect(jsonPath("$.nationalId").value("66778899"))
                .andExpect(jsonPath("$.birthDate").value(MINOR_BIRTH_DATE.toString()))
                .andExpect(jsonPath("$.age").value(Period.between(MINOR_BIRTH_DATE, LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.tutor.id").exists())
                .andExpect(jsonPath("$.tutor.firstname").value("Rosa"))
                .andExpect(jsonPath("$.tutor.lastname").value("Diaz"))
                .andExpect(jsonPath("$.tutor.cuil").value("27-99887766-4"))
                .andExpect(jsonPath("$.tutor.relation").value("MOTHER"))
                .andExpect(jsonPath("$.insuranceName").doesNotExist())
                .andExpect(jsonPath("$.insuranceNumber").doesNotExist());
    }

    @Test
    void createAdultPatientWithInsuranceShouldReturn201() throws Exception {
        LocalDate birthDate = LocalDate.of(1980, 7, 10);
        PatientCreateRequest request = new PatientCreateRequest(
                "Jorge", "Ruiz", "22334455",
                birthDate, "77778888",
                null, null, savedInsurance.getId(), "INS123"
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", containsString("/api/v1/patients/")))
                .andExpect(jsonPath("$.patientId").exists())
                .andExpect(jsonPath("$.firstname").value("Jorge"))
                .andExpect(jsonPath("$.lastname").value("Ruiz"))
                .andExpect(jsonPath("$.nationalId").value("22334455"))
                .andExpect(jsonPath("$.birthDate").value(birthDate.toString()))
                .andExpect(jsonPath("$.age").value(Period.between(birthDate, LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.phone").value("77778888"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.tutor").doesNotExist())
                .andExpect(jsonPath("$.insuranceName").value("IPS"))
                .andExpect(jsonPath("$.insuranceNumber").value("INS123"));
    }

    @Test
    void createPatientShouldReturn409WhenNationalIdAlreadyExists() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Carlos", "Gomez", "12345678",
                LocalDate.of(1985, 3, 15), null,
                null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Patient with this national ID already exists"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createPatientShouldReturn409WhenBothTutorIdAndTutorProvided() throws Exception {
        TutorCreateRequest tutorReq = new TutorCreateRequest(
                "Rosa", "Diaz", "99887766", "27-99887766-4", TutorRelation.MOTHER
        );
        PatientCreateRequest request = new PatientCreateRequest(
                "Tomás", "Diaz", "66778899",
                MINOR_BIRTH_DATE, null,
                savedTutor.getId(), tutorReq, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Provide either tutorId or tutor info, not both"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createMinorPatientWithoutTutorShouldReturn400() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Lucía", "Perez", "55443322",
                MINOR_BIRTH_DATE, null,
                null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Minor patients require tutor information"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createPatientShouldReturn404WhenTutorIdDoesNotExist() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Lucía", "Perez", "55443322",
                MINOR_BIRTH_DATE, null,
                99999L, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
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
    void createPatientShouldReturn404WhenInsuranceIdDoesNotExist() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Carlos", "Gomez", "11223344",
                LocalDate.of(1985, 3, 15), null,
                null, null, 99999L, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Insurance not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createPatientShouldReturn400WhenFirstnameIsBlank() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "", "Gomez", "11223344",
                LocalDate.of(1985, 3, 15), null,
                null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
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
                "Carlos", "", "11223344",
                LocalDate.of(1985, 3, 15), null,
                null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The last name cannot be empty")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createPatientShouldReturn400WhenNationalIdIsInvalid() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
                "Carlos", "Gomez", "ABC",
                LocalDate.of(1985, 3, 15), null,
                null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
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
                "Carlos", "Gomez", "11223344",
                null, null,
                null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
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
                "Carlos", "Gomez", "11223344",
                LocalDate.now().plusDays(1), null,
                null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The birth date cannot be in the future")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PATCH /{id} ──────────────────────────────────────────────────────────

    @Test
    void updatePatientShouldReturn200WithBasicFields() throws Exception {
        PatientUpdateRequest request = new PatientUpdateRequest(
                "ManuelUpdated", "SuarezUpdated", null, null, "99998888",
                null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedPatient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.patientId").value(savedPatient.getId()))
                .andExpect(jsonPath("$.firstname").value("ManuelUpdated"))
                .andExpect(jsonPath("$.lastname").value("SuarezUpdated"))
                .andExpect(jsonPath("$.nationalId").value("12345678"))
                .andExpect(jsonPath("$.birthDate").value(ADULT_BIRTH_DATE.toString()))
                .andExpect(jsonPath("$.age").value(Period.between(ADULT_BIRTH_DATE, LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.phone").value("99998888"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.tutor").doesNotExist())
                .andExpect(jsonPath("$.insuranceName").doesNotExist())
                .andExpect(jsonPath("$.insuranceNumber").doesNotExist());
    }

    @Test
    void updatePatientShouldReturn200WithOnlyOneField() throws Exception {
        PatientUpdateRequest request = new PatientUpdateRequest(
                "ManuelUpdated", null, null, null, null,
                null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedPatient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.patientId").value(savedPatient.getId()))
                .andExpect(jsonPath("$.firstname").value("ManuelUpdated"))
                .andExpect(jsonPath("$.lastname").value("Suarez"))
                .andExpect(jsonPath("$.nationalId").value("12345678"))
                .andExpect(jsonPath("$.birthDate").value(ADULT_BIRTH_DATE.toString()))
                .andExpect(jsonPath("$.age").value(Period.between(ADULT_BIRTH_DATE, LocalDate.now()).getYears()))
                .andExpect(jsonPath("$.phone").value("11112222"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.tutor").doesNotExist())
                .andExpect(jsonPath("$.insuranceName").doesNotExist())
                .andExpect(jsonPath("$.insuranceNumber").doesNotExist());
    }

    @Test
    void updatePatientShouldReturn404WhenNotFound() throws Exception {
        PatientUpdateRequest request = new PatientUpdateRequest(
                "Carlos", null, null, null, null,
                null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/99999")
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
    void updatePatientShouldReturn409WhenNationalIdAlreadyExists() throws Exception {
        PatientUpdateRequest request = new PatientUpdateRequest(
                null, null, "87654321", null, null,
                null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedPatient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Patient with this national ID already exists"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePatientShouldReturn409WhenBothTutorIdAndTutorProvided() throws Exception {
        TutorCreateRequest tutorReq = new TutorCreateRequest(
                "Rosa", "Diaz", "99887766", "27-99887766-4", TutorRelation.MOTHER
        );
        PatientUpdateRequest request = new PatientUpdateRequest(
                null, null, null, null, null,
                savedTutor.getId(), tutorReq, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedPatient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Provide either tutorId or tutor info, not both"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePatientShouldReturn400WhenNationalIdIsInvalid() throws Exception {
        PatientUpdateRequest request = new PatientUpdateRequest(
                null, null, "INVALID", null, null,
                null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedPatient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid DNI")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePatientShouldReturn400WhenBirthDateIsInFuture() throws Exception {
        PatientUpdateRequest request = new PatientUpdateRequest(
                null, null, null, LocalDate.now().plusDays(1), null,
                null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedPatient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The date cannot be in the future")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePatientShouldReturn400WhenNewBirthDateMakesPatientMinorAndNoTutorProvided() throws Exception {
        PatientUpdateRequest request = new PatientUpdateRequest(
                null, null, null, LocalDate.now().minusYears(10), null,
                null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedPatient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Minor patients require tutor information"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PUT /{id}/discharge ──────────────────────────────────────────────────

    @Test
    void dischargePatientShouldReturn200AndUpdateStatus() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + savedPatient.getId() + "/discharge"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.patientId").value(savedPatient.getId()))
                .andExpect(jsonPath("$.status").value("DISCHARGED"));
    }

    @Test
    void dischargePatientShouldReturn409WhenAlreadyDischarged() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + savedDischargedPatient.getId() + "/discharge"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Patient is already discharged"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void dischargePatientShouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(put(BASE_URL + "/99999/discharge"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PUT /{id}/reactive ───────────────────────────────────────────────────

    @Test
    void reactivePatientShouldReturn200AndUpdateStatus() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + savedDischargedPatient.getId() + "/reactive"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.patientId").value(savedDischargedPatient.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void reactivePatientShouldReturn409WhenAlreadyActive() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + savedPatient.getId() + "/reactive"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Patient is already active"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void reactivePatientShouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(put(BASE_URL + "/99999/reactive"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
