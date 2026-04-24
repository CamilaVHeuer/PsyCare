package com.camicompany.PsyCare.mapper;

import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentCreateRequest;
import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentResponse;
import com.camicompany.PsyCare.model.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        Long patientId = null;
        String fullName;

        if (appointment.getPatient() != null) {
            patientId = appointment.getPatient().getId();
            fullName = appointment.getPatient().getFirstname() + " " + appointment.getPatient().getLastname();
        } else {
            fullName = appointment.getPatientFirstName() + " " + appointment.getPatientLastName();
        }
        String phone;

        if (appointment.getPatient() != null) {
            phone = appointment.getPatient().getPhone();
        } else {
            phone = appointment.getPatientPhone();
        }

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getAppDateTime(),
                appointment.getPrice(),
                patientId,
                fullName,
                phone,
                appointment.getType(),
                appointment.getStatus(),
                appointment.getAppointmentPaymentStatus()
        );
    }

    public Appointment toEntity(AppointmentCreateRequest request) {
        if (request == null) {
            return null;
        }

        Appointment appointment = new Appointment();
            appointment.setAppDateTime(request.appDateTime());
            appointment.setPrice(request.price());
            appointment.setType(request.type());
        return appointment;
    }


}
