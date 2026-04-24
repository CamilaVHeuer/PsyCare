package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentCreateRequest;
import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentResponse;
import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentUpdateRequest;

public interface AppointmentService {

    public AppointmentResponse getAppointmentById( Long id);

    public AppointmentResponse createAppointment(AppointmentCreateRequest appointment);

    public AppointmentResponse updateAppointment(Long id, AppointmentUpdateRequest appointment);

    public AppointmentResponse cancelAppointment(Long id);

    public AppointmentResponse markAsAttended(Long id);

    public AppointmentResponse markAsNoShow(Long id);



}
