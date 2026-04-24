package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentCreateRequest;
import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentResponse;
import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.exception.StatusConflictException;
import com.camicompany.PsyCare.mapper.AppointmentMapper;
import com.camicompany.PsyCare.model.*;
import com.camicompany.PsyCare.repository.AppointmentRepository;
import com.camicompany.PsyCare.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppointmentServiceImp implements AppointmentService {
    private final AppointmentRepository appointmentRepo;
    private final PatientRepository patientRepo;
    private final AppointmentMapper appointmentMapper;
    private final AgendaService agendaService;

    public AppointmentServiceImp(AppointmentRepository appointmentRepo, PatientRepository patientRepo, AppointmentMapper appointmentMapper, AgendaService agendaService) {
        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
        this.appointmentMapper = appointmentMapper;
        this.agendaService = agendaService;
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    public AppointmentResponse createAppointment(AppointmentCreateRequest appointment) {
        if (!agendaService.isSlotAvailable(appointment.appDateTime())) {
            throw new StatusConflictException("This time slot is already booked");
        }
        validatePatientInputForCreate(appointment.patientId(), appointment.firstname(), appointment.lastname(), appointment.phone());

        Appointment newAppointment = appointmentMapper.toEntity(appointment);
        newAppointment.setAppointmentPaymentStatus(AppointmentPaymentStatus.PENDING);
        newAppointment.setStatus(AppointmentStatus.SCHEDULED);

        assignPatientData(newAppointment, appointment);

        Appointment savedAppointment = appointmentRepo.save(newAppointment);
        return appointmentMapper.toResponse(savedAppointment);
    }

    @Override
    public AppointmentResponse updateAppointment(Long id, AppointmentUpdateRequest appointment) {
        Appointment existingAppointment = findAppointmentOrThrow(id);

        if (appointment.appDateTime() != null &&
                appointmentRepo.existsByAppDateTimeAndIdNotAndStatusNot(
                        appointment.appDateTime(),
                        id,
                        AppointmentStatus.CANCELLED)) {

            throw new StatusConflictException("This time slot is already booked");
        }

        validateBasicFields(existingAppointment, appointment);
        validatePatientInputForUpdate(appointment.patientId(), appointment.firstname(), appointment.lastname(), appointment.phone());
        assignPatientDataUpdate(existingAppointment, appointment);

        Appointment updatedAppointment = appointmentRepo.save(existingAppointment);
        return appointmentMapper.toResponse(updatedAppointment);
    }

    @Override
    public AppointmentResponse cancelAppointment(Long id) {
        Appointment existingAppointment = findAppointmentOrThrow(id);
        if(existingAppointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new StatusConflictException("Appointment is already cancelled");
        }
        existingAppointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment canceledAppointment = appointmentRepo.save(existingAppointment);
        return appointmentMapper.toResponse(canceledAppointment);
    }

    @Override
    public AppointmentResponse markAsAttended(Long id) {
        Appointment existingAppointment = findAppointmentOrThrow(id);
        if(existingAppointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new StatusConflictException("Cannot mark a cancelled appointment as attended");
        }
        if(existingAppointment.getStatus() == AppointmentStatus.ATTENDED) {
            throw new StatusConflictException("Appointment is already marked as attended");
        }
        existingAppointment.setStatus(AppointmentStatus.ATTENDED);
        Appointment attendedAppointment = appointmentRepo.save(existingAppointment);
        return appointmentMapper.toResponse(attendedAppointment);
    }

    @Override
    public AppointmentResponse markAsNoShow(Long id) {
        Appointment existingAppointment = findAppointmentOrThrow(id);
        if(existingAppointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new StatusConflictException("Cannot mark a cancelled appointment as no-show");
        }
        if(existingAppointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new StatusConflictException("Appointment is already marked as no-show");
        }
        existingAppointment.setStatus(AppointmentStatus.NO_SHOW);
        Appointment noShowAppointment = appointmentRepo.save(existingAppointment);
        return appointmentMapper.toResponse(noShowAppointment);
    }

    //helper methods
    private void validatePatientInputForCreate(Long patientId, String firstname, String lastname, String phone) {

        boolean hasPatientId = patientId != null;
        boolean hasBasicData = firstname != null || lastname != null || phone != null;

        if (hasPatientId && hasBasicData) {
            throw new StatusConflictException("Provide either patientId or patient data, not both");
        }

        if (!hasPatientId) {
            if (firstname == null || lastname == null) {
                throw new StatusConflictException("Firstname and lastname are required when patientId is null");
            }
        }
    }

    private void validatePatientInputForUpdate(Long patientId, String firstname, String lastname, String phone) {
        boolean hasPatientId = patientId != null;
        boolean hasBasicData = firstname != null || lastname != null || phone != null;

        if (hasPatientId && hasBasicData) {
            throw new StatusConflictException("Provide either patientId or patient data, not both");
        }
    }

    private void assignPatientData(Appointment appointment, AppointmentCreateRequest request) {

        if (request.patientId() != null) {
            Patient patient = patientRepo.findById(request.patientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.patientId()));

            if (patient.getStatus() == PatientStatus.DISCHARGED) {
                throw new StatusConflictException("Cannot create appointment for a discharged patient");
            }

            appointment.setPatient(patient);
            appointment.setPatientFirstName(patient.getFirstname());
            appointment.setPatientLastName(patient.getLastname());
            appointment.setPatientPhone(patient.getPhone());

        } else {
            appointment.setPatient(null);
            appointment.setPatientFirstName(request.firstname());
            appointment.setPatientLastName(request.lastname());
            appointment.setPatientPhone(request.phone());
        }
    }

    private Appointment findAppointmentOrThrow(Long id) {
        return appointmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    private void validateBasicFields(Appointment appointment, AppointmentUpdateRequest request) {
        if (request.appDateTime() != null) appointment.setAppDateTime(request.appDateTime());
        if (request.price() != null) appointment.setPrice(request.price());
        if (request.type() != null) appointment.setType(request.type());
    }



    private void assignPatientDataUpdate(Appointment appointment, AppointmentUpdateRequest request) {
        if (request.patientId() != null) {
            Patient patient = patientRepo.findById(request.patientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.patientId()));
            if (patient.getStatus() == PatientStatus.DISCHARGED) {
                throw new StatusConflictException("Cannot create appointment for discharged patient");
            }
            appointment.setPatient(patient);
            appointment.setPatientFirstName(patient.getFirstname());
            appointment.setPatientLastName(patient.getLastname());
            appointment.setPatientPhone(patient.getPhone());

        } else if (
                request.firstname() != null ||
                        request.lastname() != null ||
                        request.phone() != null
        ) {

            if (request.firstname() != null) appointment.setPatientFirstName(request.firstname());
            if (request.lastname() != null) appointment.setPatientLastName(request.lastname());
            if (request.phone() != null) appointment.setPatientPhone(request.phone());

            appointment.setPatient(null);
        }
    }
}
