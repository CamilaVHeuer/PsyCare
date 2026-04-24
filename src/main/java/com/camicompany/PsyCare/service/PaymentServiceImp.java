package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.paymentDTO.PaymentCreateRequest;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentResponse;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.mapper.PaymentMapper;
import com.camicompany.PsyCare.model.Appointment;
import com.camicompany.PsyCare.model.Payment;
import com.camicompany.PsyCare.model.AppointmentPaymentStatus;
import com.camicompany.PsyCare.model.PaymentStatus;
import com.camicompany.PsyCare.repository.AppointmentRepository;
import com.camicompany.PsyCare.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PaymentServiceImp implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final AppointmentRepository appointmentRepo;
    private final PaymentMapper paymentMapper;

    public PaymentServiceImp(PaymentRepository paymentRepo, AppointmentRepository appointmentRepo, PaymentMapper paymentMapper) {
        this.paymentRepo = paymentRepo;
        this.appointmentRepo = appointmentRepo;
        this.paymentMapper = paymentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPaymentsByDate(LocalDate star, LocalDate end) {
        return paymentRepo.findByPayDateBetween(star, end)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    public List<PaymentResponse> getPaymentsByAppointmentId(Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId).orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
        return appointment.getPayments()
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    public PaymentResponse registerPayment(Long appointmentId, PaymentCreateRequest payment) {

        Appointment appointment = appointmentRepo.findById(appointmentId).orElseThrow(
                () -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        Payment paymentEntity = paymentMapper.toEntity(payment);
        paymentEntity.setPaymentStatus(PaymentStatus.CREATED);
        //set the appointment for the payment and add the payment to the appointment's payment list
        paymentEntity.setAppointment(appointment);
        //memory consistence
        appointment.getPayments().add(paymentEntity);
        Payment savedPayment = paymentRepo.save(paymentEntity);
        //update the appointment payment status after saving the payment
        updateAppointmentPaymentStatus(appointment);
        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public PaymentResponse updatePayment(Long id, PaymentUpdateRequest payment) {
        Payment existingPayment = findPaymentOrThrow(id);
        if(payment.payDate() != null) {
            existingPayment.setPayDate(payment.payDate());
        }
        if(payment.amount() != null) {
            existingPayment.setAmount(payment.amount());
        }
        if(payment.payMethod() != null) {
            existingPayment.setPayMethod(payment.payMethod());
        }
        Payment updatedPayment = paymentRepo.save(existingPayment);
        updateAppointmentPaymentStatus(existingPayment.getAppointment());
        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    public PaymentResponse cancelPayment(Long id) {
        Payment existingPayment = findPaymentOrThrow(id);
        existingPayment.setPaymentStatus(PaymentStatus.CANCELED);
        Payment canceledPayment = paymentRepo.save(existingPayment);
        updateAppointmentPaymentStatus(existingPayment.getAppointment());
        return paymentMapper.toResponse(canceledPayment);
    }

    //helper methods
    private void updateAppointmentPaymentStatus(Appointment appointment) {

        BigDecimal totalPaid = appointment.getPayments()
                .stream()
                .filter(p -> p.getPaymentStatus() != PaymentStatus.CANCELED)
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //Reduce is an accumulator that takes a binary operator and applies it cumulatively to the elements of the stream, resulting in a single value.
        // In this case, it sums up all the payment amounts starting from BigDecimal.ZERO.

        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            appointment.setAppointmentPaymentStatus(AppointmentPaymentStatus.PENDING);
        } else if (totalPaid.compareTo(appointment.getPrice()) < 0) {
            appointment.setAppointmentPaymentStatus(AppointmentPaymentStatus.PARTIALLY_PAID);
        } else {
            appointment.setAppointmentPaymentStatus(AppointmentPaymentStatus.PAID);
        }

        appointmentRepo.save(appointment);
    }

    private Payment findPaymentOrThrow(Long id) {
        return paymentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }
}
