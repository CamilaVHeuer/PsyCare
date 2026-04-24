package com.camicompany.PsyCare.mapper;

import com.camicompany.PsyCare.dto.paymentDTO.PaymentCreateRequest;
import com.camicompany.PsyCare.dto.paymentDTO.PaymentResponse;
import com.camicompany.PsyCare.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        return new PaymentResponse(
                payment.getId(),
                payment.getPayDate(),
                payment.getAmount(),
                payment.getPayMethod(),
                payment.getPaymentStatus(),
                payment.getAppointment() != null ? payment.getAppointment().getId() : null
        );
    }

    public Payment toEntity(PaymentCreateRequest request) {
        if (request == null) {
            return null;
        }
        Payment payment = new Payment();
        payment.setPayDate(request.payDate());
        payment.setAmount(request.amount());
        payment.setPayMethod(request.payMethod());
        return payment;
    }
}
