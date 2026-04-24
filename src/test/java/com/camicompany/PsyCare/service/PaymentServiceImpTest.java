package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.model.PaymentStatus;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentCreateRequest;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentResponse;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.mapper.PaymentMapper;
import com.camicompany.PsyCare.model.Appointment;
import com.camicompany.PsyCare.model.Payment;
import com.camicompany.PsyCare.model.AppointmentPaymentStatus;
import com.camicompany.PsyCare.repository.AppointmentRepository;
import com.camicompany.PsyCare.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImpTest {

    @Mock private PaymentRepository paymentRepo;
    @Mock private AppointmentRepository appointmentRepo;

    private PaymentServiceImp paymentService;

    @BeforeEach
    void setUp() {
        PaymentMapper paymentMapper = new PaymentMapper();
        paymentService = new PaymentServiceImp(paymentRepo, appointmentRepo, paymentMapper);
    }

    @Test
    void shouldReturnPaymentById() {
        Appointment appointment = new Appointment();
        appointment.setId(10L);
        Payment payment = createPayment(1L, LocalDate.of(2026, 4, 15), BigDecimal.valueOf(30000), "Bank transfer", appointment);

        when(paymentRepo.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(LocalDate.of(2026, 4, 15), response.payDate());
        assertEquals(BigDecimal.valueOf(30000), response.amount());
        assertEquals("Bank transfer", response.payMethod());
        assertEquals(10L, response.appointmentId());
        verify(paymentRepo).findById(1L);
    }

    @Test
    void shouldThrowResourceNotFoundWhenPaymentDoesNotExist() {
        when(paymentRepo.findById(99L)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentById(99L));
        assertEquals("Payment not found with id: 99", ex.getMessage());
        verify(paymentRepo).findById(99L);
    }

    @Test
    void shouldReturnAllPaymentsByDate() {
        Appointment appointment = new Appointment();
        appointment.setId(10L);

        Payment p1 = createPayment(1L, LocalDate.of(2026, 4, 10), BigDecimal.valueOf(10000), "Cash", appointment);
        Payment p2 = createPayment(2L, LocalDate.of(2026, 4, 20), BigDecimal.valueOf(20000), "Bank transfer", appointment);
        when(paymentRepo.findByPayDateBetween(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)))
                .thenReturn(List.of(p1, p2));

        List<PaymentResponse> responses = paymentService.getAllPaymentsByDate(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).id());
        assertEquals(LocalDate.of(2026, 4, 10), responses.get(0).payDate());
        assertEquals(BigDecimal.valueOf(10000), responses.get(0).amount());
        assertEquals(10L, responses.get(0).appointmentId());
        assertEquals(2L, responses.get(1).id());
        assertEquals(LocalDate.of(2026, 4, 20), responses.get(1).payDate());
        assertEquals(BigDecimal.valueOf(20000), responses.get(1).amount());
        assertEquals(10L, responses.get(1).appointmentId());
        verify(paymentRepo).findByPayDateBetween(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
    }

    @Test
    void shouldReturnPaymentsByAppointmentId() {
        Long appointmentId = 10L;
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);

        Payment p1 = createPayment(1L, LocalDate.of(2026, 4, 10), BigDecimal.valueOf(10000), "Cash", appointment);
        Payment p2 = createPayment(2L, LocalDate.of(2026, 4, 15), BigDecimal.valueOf(20000),"Bank transfer", appointment);

        appointment.setPayments(List.of(p1, p2));

        when(appointmentRepo.findById(appointmentId)).thenReturn(Optional.of(appointment));

        List<PaymentResponse> result = paymentService.getPaymentsByAppointmentId(appointmentId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(LocalDate.of(2026, 4, 10), result.get(0).payDate());
        assertEquals(BigDecimal.valueOf(10000), result.get(0).amount());
        assertEquals(10L, result.get(0).appointmentId());
        assertEquals(2L, result.get(1).id());
        assertEquals(LocalDate.of(2026, 4, 15), result.get(1).payDate());
        assertEquals(BigDecimal.valueOf(20000), result.get(1).amount());
        assertEquals(10L, result.get(1).appointmentId());

        verify(appointmentRepo).findById(appointmentId);
    }

    @Test
    void shouldThrowWhenGettingPaymentsForNonexistentAppointment() {
        Long appointmentId = 99L;

        when(appointmentRepo.findById(appointmentId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPaymentsByAppointmentId(appointmentId));
        assertEquals("Appointment not found with id: 99", ex.getMessage());
    }

    @Test
    void shouldRegisterPaymentSuccessfullyAndUpdateAppointmentStatus() {
        Long appointmentId = 10L;
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setPrice(BigDecimal.valueOf(30000));
        appointment.setPayments(new ArrayList<>());
        appointment.setAppointmentPaymentStatus(AppointmentPaymentStatus.PENDING);

        PaymentCreateRequest request = new PaymentCreateRequest(LocalDate.of(2026, 4, 15), BigDecimal.valueOf(30000), "Bank transfer");
        Payment savedPayment = createPayment(1L, request.payDate(), request.amount(), request.payMethod(), appointment);

        when(appointmentRepo.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(paymentRepo.save(any(Payment.class))).thenReturn(savedPayment);
        when(appointmentRepo.save(any(Appointment.class))).thenReturn(appointment);

        PaymentResponse response = paymentService.registerPayment(appointmentId, request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(request.payDate(), response.payDate());
        assertEquals(request.amount(), response.amount());
        assertEquals(request.payMethod(), response.payMethod());
        assertEquals(appointmentId, response.appointmentId());

        // Verify that the payment was added to the appointment's payment list
        assertTrue(appointment.getPayments().contains(savedPayment) || appointment.getPayments().size() == 1);

        // Verify that the appointment payment status was updated to PAID
        assertEquals(AppointmentPaymentStatus.PAID, appointment.getAppointmentPaymentStatus());

        verify(appointmentRepo).findById(appointmentId);
        verify(paymentRepo).save(any(Payment.class));
        verify(appointmentRepo).save(appointment);
    }

    @Test
    void shouldThrowResourceNotFoundWhenRegisterPaymentWithNonexistentAppointment() {
        Long appointmentId = 99L;
        PaymentCreateRequest request = new PaymentCreateRequest(LocalDate.of(2026, 4, 15), BigDecimal.valueOf(2000), "Bank transfer");
        when(appointmentRepo.findById(appointmentId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> paymentService.registerPayment(appointmentId, request));
        assertEquals("Appointment not found with id: 99", ex.getMessage());
        verify(appointmentRepo).findById(appointmentId);
        verify(paymentRepo, never()).save(any());
    }

    @Test
    void shouldUpdatePaymentAndRecalculateAppointmentStatus() {
        Long paymentId = 1L;

        Appointment appointment = new Appointment();
        appointment.setId(10L);
        appointment.setPrice(BigDecimal.valueOf(30000));
        appointment.setPayments(new ArrayList<>());
        appointment.setAppointmentPaymentStatus(AppointmentPaymentStatus.PENDING);

        Payment existingPayment = createPayment(paymentId, LocalDate.of(2026, 4, 10),
                BigDecimal.valueOf(10000), "Cash", appointment);

        appointment.getPayments().add(existingPayment);

        PaymentUpdateRequest updateRequest =
                new PaymentUpdateRequest(null, BigDecimal.valueOf(30000), null);

        when(paymentRepo.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepo.save(any())).thenReturn(existingPayment);
        when(appointmentRepo.save(any())).thenReturn(appointment);

        PaymentResponse response = paymentService.updatePayment(paymentId, updateRequest);


        assertEquals(BigDecimal.valueOf(30000), existingPayment.getAmount());
        assertEquals(AppointmentPaymentStatus.PAID, appointment.getAppointmentPaymentStatus());

        assertNotNull(response);
    }

    @Test
    void shouldThrowResourceNotFoundWhenUpdatePaymentWithNonexistentPayment() {
        Long paymentId = 99L;
        PaymentUpdateRequest updateRequest = new PaymentUpdateRequest(LocalDate.of(2026, 4, 20), BigDecimal.valueOf(20000), "Bank transfer");
        when(paymentRepo.findById(paymentId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> paymentService.updatePayment(paymentId, updateRequest));
        assertEquals("Payment not found with id: 99", ex.getMessage());
        verify(paymentRepo).findById(paymentId);
        verify(paymentRepo, never()).save(any());
    }

    @Test
    void shouldCancelPaymentAndUpdateAppointmentStatus() {
        Long paymentId = 1L;

        Appointment appointment = new Appointment();
        appointment.setId(10L);
        appointment.setPrice(BigDecimal.valueOf(20000));
        appointment.setPayments(new ArrayList<>());

        Payment payment = createPayment(paymentId, LocalDate.now(), BigDecimal.valueOf(20000),
                "Bank transfer", appointment);

        appointment.getPayments().add(payment);

        when(paymentRepo.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepo.save(any())).thenReturn(payment);
        when(appointmentRepo.save(any())).thenReturn(appointment);

        PaymentResponse response = paymentService.cancelPayment(paymentId);

        assertEquals(PaymentStatus.CANCELED, payment.getPaymentStatus());
        assertEquals(AppointmentPaymentStatus.PENDING, appointment.getAppointmentPaymentStatus());
        assertNotNull(response);
    }

    @Test
    void shouldThrowWhenCancelingNonexistentPayment() {
        when(paymentRepo.findById(99L)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> paymentService.cancelPayment(99L));
        assertEquals("Payment not found with id: 99", ex.getMessage());
        verify(paymentRepo).findById(99L);
    }

    @Test
    void shouldSetAppointmentAsPartiallyPaid() {
        Appointment appointment = new Appointment();
        appointment.setId(10L);
        appointment.setPrice(BigDecimal.valueOf(30000));
        appointment.setPayments(new ArrayList<>());

        Payment p1 = createPayment(1L, LocalDate.now(), BigDecimal.valueOf(25000), "Cash", appointment);
        Payment p2 = createPayment(2L, LocalDate.now(), BigDecimal.valueOf(5000), "Bank transfer", appointment);

        appointment.getPayments().add(p1);
        appointment.getPayments().add(p2);

        when(paymentRepo.findById(1L)).thenReturn(Optional.of(p1));
        when(paymentRepo.save(any())).thenReturn(p1);
        when(appointmentRepo.save(any())).thenReturn(appointment);

        PaymentUpdateRequest update = new PaymentUpdateRequest(null, BigDecimal.valueOf(10000), null);

        paymentService.updatePayment(1L, update);

        assertEquals(AppointmentPaymentStatus.PARTIALLY_PAID, appointment.getAppointmentPaymentStatus());
    }

    @Test
    void canceledPaymentsShouldNotAffectTotal() {
        Appointment appointment = new Appointment();
        appointment.setId(10L);
        appointment.setPrice(BigDecimal.valueOf(20000));
        appointment.setPayments(new ArrayList<>());

        Payment p1 = createPayment(1L, LocalDate.now(), BigDecimal.valueOf(20000), "Cash", appointment);
        p1.setPaymentStatus(PaymentStatus.CANCELED);

        appointment.getPayments().add(p1);

        when(paymentRepo.findById(1L)).thenReturn(Optional.of(p1));
        when(paymentRepo.save(any())).thenReturn(p1);
        when(appointmentRepo.save(any())).thenReturn(appointment);

        paymentService.cancelPayment(1L);

        assertEquals(AppointmentPaymentStatus.PENDING, appointment.getAppointmentPaymentStatus());
    }

    // Helper
    private Payment createPayment(Long id, LocalDate payDate, BigDecimal amount, String payMethod, Appointment appointment) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setPayDate(payDate);
        payment.setAmount(amount);
        payment.setPayMethod(payMethod);
        payment.setAppointment(appointment);
        return payment;
    }

}
