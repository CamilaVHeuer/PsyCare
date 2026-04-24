package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.paymentDTO.PaymentCreateRequest;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentResponse;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentUpdateRequest;

import java.time.LocalDate;
import java.util.List;

public interface PaymentService {
    public PaymentResponse getPaymentById(Long id);

    //range of dates
    public List<PaymentResponse> getAllPaymentsByDate( LocalDate star, LocalDate end);
    public List<PaymentResponse> getPaymentsByAppointmentId(Long appointmentId);
    public PaymentResponse registerPayment(Long appointmentID, PaymentCreateRequest payment);

    public PaymentResponse updatePayment(Long id, PaymentUpdateRequest payment);
    public PaymentResponse cancelPayment(Long id);
}
