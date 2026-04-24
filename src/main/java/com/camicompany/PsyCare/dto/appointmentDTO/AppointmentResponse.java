package com.camicompany.PsyCare.dto.appointmentDTO;

import com.camicompany.PsyCare.model.AppointmentStatus;
import com.camicompany.PsyCare.model.AppointmentType;
import com.camicompany.PsyCare.model.AppointmentPaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        LocalDateTime appDateTime,
        BigDecimal price,
        Long patientId,
        String patientFullName,
        String patientPhone,
        AppointmentType type,
        AppointmentStatus status,
        AppointmentPaymentStatus appointmentPaymentStatus) {
}
