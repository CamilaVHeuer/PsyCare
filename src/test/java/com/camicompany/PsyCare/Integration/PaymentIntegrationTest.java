package com.camicompany.PsyCare.Integration;

import com.camicompany.PsyCare.dto.paymentDTO.PaymentCreateRequest;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentUpdateRequest;
import com.camicompany.PsyCare.model.*;
import com.camicompany.PsyCare.repository.AppointmentRepository;
import com.camicompany.PsyCare.repository.PatientRepository;
import com.camicompany.PsyCare.repository.PaymentRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class PaymentIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private PatientRepository patientRepository;

    private static final String BASE_URL = "/api/v1";

    private Appointment savedAppointment;
    private Payment savedPayment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();

        Patient patient = new Patient();
        patient.setFirstname("Manuel");
        patient.setLastname("Suarez");
        patient.setNationalId("12345678");
        patient.setPhone("11112222");
        patient.setStatus(PatientStatus.ACTIVE);
        patientRepository.save(patient);

        Appointment appointment = new Appointment();
        appointment.setAppDateTime(LocalDateTime.of(2026, 11, 10, 14, 0));
        appointment.setPrice(BigDecimal.valueOf(30000));
        appointment.setPatientFirstName("Manuel");
        appointment.setPatientLastName("Suarez");
        appointment.setPatientPhone("11112222");
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setAppointmentPaymentStatus(AppointmentPaymentStatus.PENDING);
        appointment.setType(AppointmentType.GENERAL);
        appointment.setPatient(patient);
        savedAppointment = appointmentRepository.save(appointment);

        Payment payment = new Payment();
        payment.setPayDate(LocalDate.of(2026, 4, 15));
        payment.setAmount(BigDecimal.valueOf(10000));
        payment.setPayMethod("Cash");
        payment.setPaymentStatus(PaymentStatus.CREATED);
        payment.setAppointment(savedAppointment);
        savedPayment = paymentRepository.save(payment);
        savedAppointment.getPayments().add(payment);
    }

    // ── GET /payments/{id} ───────────────────────────────────────────────────

    @Test
    void getPaymentByIdShouldReturn200WhenExists() throws Exception {
        mockMvc.perform(get(BASE_URL + "/payments/" + savedPayment.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedPayment.getId()))
                .andExpect(jsonPath("$.payDate").value("2026-04-15"))
                .andExpect(jsonPath("$.amount").value(10000))
                .andExpect(jsonPath("$.payMethod").value("Cash"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.appointmentId").value(savedAppointment.getId()));
    }

    @Test
    void getPaymentByIdShouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/payments/99999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Payment not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── GET /appointments/{id}/payments ──────────────────────────────────────

    @Test
    void getPaymentsByAppointmentIdShouldReturn200WithList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/appointments/" + savedAppointment.getId() + "/payments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(savedPayment.getId()))
                .andExpect(jsonPath("$[0].payDate").value("2026-04-15"))
                .andExpect(jsonPath("$[0].amount").value(10000))
                .andExpect(jsonPath("$[0].payMethod").value("Cash"))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].appointmentId").value(savedAppointment.getId()));
    }

    @Test
    void getPaymentsByAppointmentIdShouldReturn404WhenAppointmentNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/appointments/99999/payments"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── GET /payments?startDate=&endDate= ─────────────────────────────────────

    @Test
    void getPaymentsByDateRangeShouldReturn200WithResults() throws Exception {
        mockMvc.perform(get(BASE_URL + "/payments")
                        .param("startDate", "2026-04-01")
                        .param("endDate", "2026-04-30"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(savedPayment.getId()))
                .andExpect(jsonPath("$[0].payDate").value("2026-04-15"))
                .andExpect(jsonPath("$[0].amount").value(10000))
                .andExpect(jsonPath("$[0].payMethod").value("Cash"))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].appointmentId").value(savedAppointment.getId()));
    }

    @Test
    void getPaymentsByDateRangeShouldReturn200WithEmptyListWhenNoneMatch() throws Exception {
        mockMvc.perform(get(BASE_URL + "/payments")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getPaymentsByDateRangeShouldReturn400WhenDateIsInvalid() throws Exception {
        mockMvc.perform(get(BASE_URL + "/payments")
                        .param("startDate", "not-a-date")
                        .param("endDate", "2026-04-30"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid parameter format"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── POST /appointments/{id}/payments ─────────────────────────────────────

    @Test
    void registerPaymentShouldReturn201WithValidRequest() throws Exception {
        LocalDate payDate = LocalDate.of(2026, 4, 20);
        PaymentCreateRequest request = new PaymentCreateRequest(
                payDate, BigDecimal.valueOf(20000), "Bank transfer"
        );

        mockMvc.perform(post(BASE_URL + "/appointments/" + savedAppointment.getId() + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", containsString("/api/v1/payments/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.payDate").value(payDate.toString()))
                .andExpect(jsonPath("$.amount").value(20000))
                .andExpect(jsonPath("$.payMethod").value("Bank transfer"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.appointmentId").value(savedAppointment.getId()));
    }

    @Test
    void registerPaymentShouldReturn404WhenAppointmentNotFound() throws Exception {
        PaymentCreateRequest request = new PaymentCreateRequest(
                LocalDate.of(2026, 4, 20), BigDecimal.valueOf(20000), "Bank transfer"
        );

        mockMvc.perform(post(BASE_URL + "/appointments/99999/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void registerPaymentShouldReturn400WhenDateIsInFuture() throws Exception {
        PaymentCreateRequest request = new PaymentCreateRequest(
                LocalDate.now().plusDays(2), BigDecimal.valueOf(20000), "Bank transfer"
        );

        mockMvc.perform(post(BASE_URL + "/appointments/" + savedAppointment.getId() + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The payment registration date cannot be in the future")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void registerPaymentShouldReturn400WhenAmountIsNegative() throws Exception {
        PaymentCreateRequest request = new PaymentCreateRequest(
                LocalDate.of(2026, 4, 20), BigDecimal.valueOf(-500), "Bank transfer"
        );

        mockMvc.perform(post(BASE_URL + "/appointments/" + savedAppointment.getId() + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The payment amount must be a positive value")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void registerPaymentShouldReturn400WhenPayMethodIsBlank() throws Exception {
        PaymentCreateRequest request = new PaymentCreateRequest(
                LocalDate.of(2026, 4, 20), BigDecimal.valueOf(20000), ""
        );

        mockMvc.perform(post(BASE_URL + "/appointments/" + savedAppointment.getId() + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The payment method is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PATCH /payments/{id} ─────────────────────────────────────────────────

    @Test
    void updatePaymentShouldReturn200WithAllFields() throws Exception {
        LocalDate newDate = LocalDate.of(2026, 4, 10);
        PaymentUpdateRequest request = new PaymentUpdateRequest(
                newDate, BigDecimal.valueOf(15000), "Bank transfer"
        );

        mockMvc.perform(patch(BASE_URL + "/payments/" + savedPayment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedPayment.getId()))
                .andExpect(jsonPath("$.payDate").value(newDate.toString()))
                .andExpect(jsonPath("$.amount").value(15000))
                .andExpect(jsonPath("$.payMethod").value("Bank transfer"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.appointmentId").value(savedAppointment.getId()));
    }

    @Test
    void updatePaymentShouldReturn200WithOnlyOneField() throws Exception {
        PaymentUpdateRequest request = new PaymentUpdateRequest(
                null, BigDecimal.valueOf(25000), null
        );

        mockMvc.perform(patch(BASE_URL + "/payments/" + savedPayment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedPayment.getId()))
                .andExpect(jsonPath("$.payDate").value("2026-04-15"))
                .andExpect(jsonPath("$.amount").value(25000))
                .andExpect(jsonPath("$.payMethod").value("Cash"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.appointmentId").value(savedAppointment.getId()));
    }

    @Test
    void updatePaymentShouldReturn404WhenNotFound() throws Exception {
        PaymentUpdateRequest request = new PaymentUpdateRequest(
                null, BigDecimal.valueOf(15000), null
        );

        mockMvc.perform(patch(BASE_URL + "/payments/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Payment not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePaymentShouldReturn400WhenDateIsInFuture() throws Exception {
        PaymentUpdateRequest request = new PaymentUpdateRequest(
                LocalDate.now().plusDays(2), null, null
        );

        mockMvc.perform(patch(BASE_URL + "/payments/" + savedPayment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The payment registration date cannot be in the future")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updatePaymentShouldReturn400WhenAmountIsNegative() throws Exception {
        PaymentUpdateRequest request = new PaymentUpdateRequest(
                null, BigDecimal.valueOf(-100), null
        );

        mockMvc.perform(patch(BASE_URL + "/payments/" + savedPayment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The payment amount must be a positive value")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── PUT /payments/{id}/cancel ─────────────────────────────────────────────

    @Test
    void cancelPaymentShouldReturn200AndUpdateStatus() throws Exception {
        mockMvc.perform(put(BASE_URL + "/payments/" + savedPayment.getId() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedPayment.getId()))
                .andExpect(jsonPath("$.payDate").value("2026-04-15"))
                .andExpect(jsonPath("$.amount").value(10000))
                .andExpect(jsonPath("$.payMethod").value("Cash"))
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.appointmentId").value(savedAppointment.getId()));
    }

    @Test
    void cancelPaymentShouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(put(BASE_URL + "/payments/99999/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Payment not found with id: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
