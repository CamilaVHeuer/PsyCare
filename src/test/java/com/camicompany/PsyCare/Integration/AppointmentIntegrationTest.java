package com.camicompany.PsyCare.Integration;

import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentCreateRequest;
import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentUpdateRequest;
import com.camicompany.PsyCare.model.*;
import com.camicompany.PsyCare.repository.AppointmentRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class AppointmentIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private PatientRepository patientRepository;

    private static final String BASE_URL = "/api/v1/appointments";

    private Patient savedPatient;
    private Appointment savedAppointment;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();

        Patient patient = new Patient();
        patient.setFirstname("Manuel");
        patient.setLastname("Suarez");
        patient.setNationalId("12345678");
        patient.setPhone("123456789");
        patient.setStatus(PatientStatus.ACTIVE);
        savedPatient = patientRepository.save(patient);

        Appointment appointment = new Appointment();
        appointment.setAppDateTime(LocalDateTime.of(2026, 11, 10, 14, 0));
        appointment.setPrice(BigDecimal.valueOf(10000));
        appointment.setPatientFirstName("Manuel");
        appointment.setPatientLastName("Suarez");
        appointment.setPatientPhone(savedPatient.getPhone());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setAppointmentPaymentStatus(AppointmentPaymentStatus.PENDING);
        appointment.setType(AppointmentType.GENERAL);
        appointment.setPatient(savedPatient);
        savedAppointment = appointmentRepository.save(appointment);
    }

    // ── GET /{id} ────────────────────────────────────────────────────────────

    @Test
    void getAppointmentByIdShouldReturn200WhenExists() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + savedAppointment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedAppointment.getId()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.type").value("GENERAL"))
                .andExpect(jsonPath("$.patientId").value(savedPatient.getId()));
    }

    @Test
    void getAppointmentByIdShouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/99999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── POST / ───────────────────────────────────────────────────────────────

    @Test
    void createAppointmentShouldReturn201WithValidRequest() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2026, 12, 1, 13, 0),
                BigDecimal.valueOf(15000),
                "Laura", "Gonzalez", "987654321",
                null,
                AppointmentType.GENERAL
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.appDateTime").value("2026-12-01T13:00:00"))
                .andExpect(jsonPath("$.price").value(15000.00))
                .andExpect(jsonPath("$.patientId").doesNotExist())
                .andExpect(jsonPath("$.patientFullName").value("Laura Gonzalez"))
                .andExpect(jsonPath("$.patientPhone").value("987654321"))
                .andExpect(jsonPath("$.type").value("GENERAL"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.appointmentPaymentStatus").value("PENDING"));

        assertEquals(2, appointmentRepository.count());;
    }

    @Test
    void createAppointmentShouldReturn400WhenMissingRequiredFields() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                null,
                BigDecimal.valueOf(15000),
                null, null, null,
                null,
                AppointmentType.GENERAL
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createAppointmentShouldReturn400WhenDateIsInThePast() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2020, 1, 1, 10, 0),
                BigDecimal.valueOf(15000),
                "Laura", "Gonzalez", "987654321",
                null,
                AppointmentType.GENERAL
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The appointment date cannot be in the past")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createAppointmentShouldReturn400WhenPriceIsNegative() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2026, 12, 1, 10, 0),
                BigDecimal.valueOf(-500),
                "Laura", "Gonzalez", "987654321",
                null,
                AppointmentType.GENERAL
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The price must be a positive number")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createAppointmentShouldReturn409WhenPatientIdAndPatientDataProvided() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2026, 12, 1, 13, 0),
                BigDecimal.valueOf(15000),
                "Laura", "Gonzalez", "987654321",
                savedPatient.getId(),
                AppointmentType.GENERAL
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Provide either patientId or patient data, not both"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createAppointmentShouldReturn409WhenSlotAlreadyBooked() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                savedAppointment.getAppDateTime(),
                BigDecimal.valueOf(15000),
                null, null, null,
                null,
                AppointmentType.GENERAL
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("already booked")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createAppointmentShouldUseExistingPatientWhenPatientIdProvided() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2026, 12, 2, 13, 0),
                BigDecimal.valueOf(15000),
                null, null, null,
                savedPatient.getId(),
                AppointmentType.GENERAL
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.patientId").value(savedPatient.getId()))
                .andExpect(jsonPath("$.patientFullName").value("Manuel Suarez"))
                .andExpect(jsonPath("$.patientPhone").value(savedPatient.getPhone()));

        assertEquals(2, appointmentRepository.count());;
    }

    @Test
    void createAppointmentShouldReturn404WhenPatientIdDoesNotExist() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2026, 12, 3, 13, 0),
                BigDecimal.valueOf(15000),
                null, null, null,
                99999L,
                AppointmentType.GENERAL
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(containsString("99999")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PATCH /{id} ──────────────────────────────────────────────────────────

    @Test
    void updateAppointmentShouldReturn409WhenNewSlotIsOccupied() throws Exception {
        // Create a second appointment at a different slot
        AppointmentCreateRequest secondRequest = new AppointmentCreateRequest(
                LocalDateTime.of(2026, 12, 4, 14, 0),
                BigDecimal.valueOf(15000),
                "Rosa", "Sanchez", "555555555",
                null,
                AppointmentType.GENERAL
        );
        String secondResponse = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long secondId = objectMapper.readTree(secondResponse).get("id").asLong();

        // Now try to update the second appointment to the same slot as savedAppointment
        AppointmentUpdateRequest updateRequest = new AppointmentUpdateRequest(
                savedAppointment.getAppDateTime(),
                null, null, null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/" + secondId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("already booked")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateAppointmentShouldReturn200WithValidRequest() throws Exception {
        AppointmentUpdateRequest request = new AppointmentUpdateRequest(
                LocalDateTime.of(2026, 12, 5, 15, 0),
                BigDecimal.valueOf(20000),
                null, null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedAppointment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedAppointment.getId()))
                .andExpect(jsonPath("$.appDateTime").value("2026-12-05T15:00:00"))
                .andExpect(jsonPath("$.price").value(20000.00))
                .andExpect(jsonPath("$.patientId").value(savedAppointment.getPatient().getId()))
                .andExpect(jsonPath("$.patientFullName").value("Manuel Suarez"))
                .andExpect(jsonPath("$.patientPhone").value(savedAppointment.getPatient().getPhone()))
                .andExpect(jsonPath("$.type").value("GENERAL"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.appointmentPaymentStatus").value("PENDING"));
    }

    @Test
    void updateAppointmentWithOnlyDateShouldKeepPatientLinked() throws Exception {
        AppointmentUpdateRequest request = new AppointmentUpdateRequest(
                LocalDateTime.of(2026, 12, 10, 16, 0),
                null,
                null, null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedAppointment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedAppointment.getId()))
                .andExpect(jsonPath("$.appDateTime").value("2026-12-10T16:00:00"))
                .andExpect(jsonPath("$.price").value(10000.00))
                .andExpect(jsonPath("$.patientId").value(savedPatient.getId()))
                .andExpect(jsonPath("$.patientFullName").value("Manuel Suarez"))
                .andExpect(jsonPath("$.patientPhone").value(savedPatient.getPhone()))
                .andExpect(jsonPath("$.type").value("GENERAL"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.appointmentPaymentStatus").value("PENDING"));
    }

    @Test
    void updateAppointmentWithPatientDataShouldUnlinkExistingPatient() throws Exception {
        AppointmentUpdateRequest request = new AppointmentUpdateRequest(
                LocalDateTime.of(2026, 12, 10, 16, 0),
                null, "Carlos", "Ramirez", "12345678", null, null
        );

        mockMvc.perform(patch(BASE_URL + "/" + savedAppointment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedAppointment.getId()))
                .andExpect(jsonPath("$.appDateTime").value("2026-12-10T16:00:00"))
                .andExpect(jsonPath("$.price").value(10000.00))
                .andExpect(jsonPath("$.patientId").isEmpty())
                .andExpect(jsonPath("$.patientFullName").value("Carlos Ramirez"))
                .andExpect(jsonPath("$.patientPhone").value("12345678"))
                .andExpect(jsonPath("$.type").value("GENERAL"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.appointmentPaymentStatus").value("PENDING"));
    }

    @Test
    void updateAppointmentShouldReturn404WhenNotFound() throws Exception {
        AppointmentUpdateRequest request = new AppointmentUpdateRequest(
                null,
                BigDecimal.valueOf(20000),
                null, null, null, null, null
        );

        mockMvc.perform(patch(BASE_URL + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PUT /{id}/cancel ─────────────────────────────────────────────────────

    @Test
    void cancelAppointmentShouldReturn200AndUpdateStatus() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + savedAppointment.getId() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelAppointmentShouldReturn409WhenAlreadyCancelled() throws Exception {
        savedAppointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(savedAppointment);

        mockMvc.perform(put(BASE_URL + "/" + savedAppointment.getId() + "/cancel"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Appointment is already cancelled"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PUT /{id}/mark-as-attended ───────────────────────────────────────────

    @Test
    void markAsAttendedShouldReturn200AndUpdateStatus() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + savedAppointment.getId() + "/mark-as-attended"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATTENDED"));
    }

    @Test
    void markAsAttendedShouldReturn409WhenAlreadyAttended() throws Exception {
        savedAppointment.setStatus(AppointmentStatus.ATTENDED);
        appointmentRepository.save(savedAppointment);

        mockMvc.perform(put(BASE_URL + "/" + savedAppointment.getId() + "/mark-as-attended"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Appointment is already marked as attended"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PUT /{id}/mark-as-no-show ────────────────────────────────────────────

    @Test
    void markAsNoShowShouldReturn200AndUpdateStatus() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + savedAppointment.getId() + "/mark-as-no-show"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));
    }

    @Test
    void markAsNoShowShouldReturn409WhenAlreadyNoShow() throws Exception {
        savedAppointment.setStatus(AppointmentStatus.NO_SHOW);
        appointmentRepository.save(savedAppointment);

        mockMvc.perform(put(BASE_URL + "/" + savedAppointment.getId() + "/mark-as-no-show"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Appointment is already marked as no-show"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
