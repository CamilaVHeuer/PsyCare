
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AppointmentServiceImpTest {

    private AppointmentService appointmentService;

    @Mock private AppointmentRepository appointmentRepo;

    @Mock private PatientRepository patientRepo;

    @Mock private AgendaService agendaService;

    @Captor private ArgumentCaptor<Appointment> captor;

    @BeforeEach
    void setUp() {
        AppointmentMapper appointmentMapper = new AppointmentMapper();
        appointmentService = new AppointmentServiceImp(appointmentRepo, patientRepo, appointmentMapper, agendaService);
    }
    @Test
    void shouldGetAppointmentByIdSuccessfully() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);

        Patient patient = new Patient();
        patient.setFirstname("Juan");
        patient.setLastname("Pérez");
        patient.setPhone("12345678");
        patient.setId(1L);

        appointment.setPatient(patient);
        appointment.setAppDateTime(LocalDateTime.of(2026, 11, 2, 13, 0));
        appointment.setPrice(BigDecimal.valueOf(30000));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setAppointmentPaymentStatus(AppointmentPaymentStatus.PENDING);
        appointment.setType(AppointmentType.GENERAL);

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(appointment));

        AppointmentResponse response = appointmentService.getAppointmentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(appointment.getAppDateTime(), response.appDateTime());
        assertEquals(appointment.getPrice(), response.price());
        assertEquals(1L, response.patientId());
        assertEquals("Juan Pérez", response.patientFullName());
        assertEquals("12345678", response.patientPhone());
        assertEquals(AppointmentType.GENERAL, response.type());
        assertEquals(AppointmentStatus.SCHEDULED, response.status());
        assertEquals(AppointmentPaymentStatus.PENDING, response.appointmentPaymentStatus());

        verify(appointmentRepo).findById(1L);
    }

    @Test
    void shouldGetAppointmentByIdSuccessfullyWhitOutPatientId() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatientFirstName("Juan");
        appointment.setPatientLastName("Pérez");
        appointment.setPatientPhone("12345678");
        appointment.setAppDateTime(LocalDateTime.of(2026, 11, 2, 13, 0));
        appointment.setPrice(BigDecimal.valueOf(30000));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setAppointmentPaymentStatus(AppointmentPaymentStatus.PENDING);
        appointment.setType(AppointmentType.GENERAL);

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(appointment));

        AppointmentResponse response = appointmentService.getAppointmentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(appointment.getAppDateTime(), response.appDateTime());
        assertEquals(appointment.getPrice(), response.price());
        assertEquals("Juan Pérez", response.patientFullName());
        assertEquals("12345678", response.patientPhone());
        assertEquals(AppointmentType.GENERAL, response.type());
        assertEquals(AppointmentStatus.SCHEDULED, response.status());
        assertEquals(AppointmentPaymentStatus.PENDING, response.appointmentPaymentStatus());
        verify(appointmentRepo).findById(1L);
    }

   @Test
    void shouldThrowResourceNotFoundWhenAppointmentDoesNotExist() {
        when(appointmentRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> appointmentService.getAppointmentById(99L));
        verify(appointmentRepo).findById(99L);
    }

    @Test
    void shouldCreateAppointmentSuccessfullyWithPatientId() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2026, 11, 2, 13, 0),
                BigDecimal.valueOf(30000),
                null, null, null,
                1L,
                AppointmentType.GENERAL
        );
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstname("Juan");
        patient.setLastname("Pérez");
        patient.setPhone("12345678");
        patient.setStatus(PatientStatus.ACTIVE);

        when(agendaService.isSlotAvailable(request.appDateTime())).thenReturn(true);
        when(patientRepo.findById(1L)).thenReturn(Optional.of(patient));
        when(appointmentRepo.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        AppointmentResponse response = appointmentService.createAppointment(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals(1L, response.patientId());
        assertEquals("Juan Pérez", response.patientFullName());
        assertEquals("12345678", response.patientPhone());
        assertEquals(AppointmentType.GENERAL, response.type());
        assertEquals(AppointmentStatus.SCHEDULED, response.status());
        assertEquals(AppointmentPaymentStatus.PENDING, response.appointmentPaymentStatus());
        verify(appointmentRepo).save(any(Appointment.class));

        verify(appointmentRepo).save(captor.capture());

        Appointment saved = captor.getValue();

        assertEquals(AppointmentPaymentStatus.PENDING, saved.getAppointmentPaymentStatus());
        assertEquals(AppointmentStatus.SCHEDULED, saved.getStatus());
    }

    @Test
    void shouldCreateAppointmentSuccessfullyWithPatientFirstnameAndLastname() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2024, 6, 1, 13, 0),
                BigDecimal.valueOf(30000),
                "Juan", "Pérez", "12345678",
                null,
                AppointmentType.GENERAL
        );

        when(agendaService.isSlotAvailable(request.appDateTime())).thenReturn(true);
        when(appointmentRepo.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        AppointmentResponse response = appointmentService.createAppointment(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertNull(response.patientId());
        assertEquals("Juan Pérez", response.patientFullName());
        assertEquals("12345678", response.patientPhone());
        assertEquals(AppointmentType.GENERAL, response.type());
        assertEquals(AppointmentStatus.SCHEDULED, response.status());
        assertEquals(AppointmentPaymentStatus.PENDING, response.appointmentPaymentStatus());
        verify(appointmentRepo).save(any(Appointment.class));

        verify(appointmentRepo).save(captor.capture());

        Appointment saved = captor.getValue();

        assertEquals(AppointmentPaymentStatus.PENDING, saved.getAppointmentPaymentStatus());
        assertEquals(AppointmentStatus.SCHEDULED, saved.getStatus());
    }


   @Test
    void shouldThrowExceptionIfNoPatientIdAndNoNameLastname() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2024, 6, 1, 13, 0),
                BigDecimal.valueOf(30000),
                null, null, null,
                null,
                AppointmentType.GENERAL
        );
        when(agendaService.isSlotAvailable(request.appDateTime())).thenReturn(true);

       var ex =  assertThrows(StatusConflictException.class, () -> appointmentService.createAppointment(request));
       assertEquals("Firstname and lastname are required when patientId is null", ex.getMessage());
       verify(appointmentRepo, never()).save(any());
    }

    @Test
    void shouldThrowIfMixedPatientDataOnCreate() {
        AppointmentCreateRequest req = new AppointmentCreateRequest(
                LocalDateTime.now().plusDays(1),
                BigDecimal.valueOf(30000),
                "Ana", "Gomez", null,
                1L,
                AppointmentType.GENERAL
        );

        when(agendaService.isSlotAvailable(any())).thenReturn(true);

        var ex = assertThrows(StatusConflictException.class,
                () -> appointmentService.createAppointment(req));
        assertEquals("Provide either patientId or patient data, not both", ex.getMessage());
    }


    @Test
    void shouldThrowExceptionIfSlotIsAlreadyBooked() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2024, 6, 1, 13, 0),
                BigDecimal.valueOf(30000),
                "Ana", "Gomez", "12345678",
                null,
                AppointmentType.GENERAL
        );
        when(agendaService.isSlotAvailable(request.appDateTime())).thenReturn(false);

        var ex =  assertThrows(StatusConflictException.class, () -> appointmentService.createAppointment(request));
        assertEquals("This time slot is already booked", ex.getMessage());
        verify(appointmentRepo, never()).save(any());
    }
    @Test
    void shouldThrowIfPatientDischarged() {
        AppointmentCreateRequest req = new AppointmentCreateRequest(
                LocalDateTime.now().plusDays(1),
                BigDecimal.valueOf(30000),
                null, null, null,
                1L,
                AppointmentType.GENERAL
        );

        Patient patient = new Patient();
        patient.setStatus(PatientStatus.DISCHARGED);

        when(agendaService.isSlotAvailable(any())).thenReturn(true);
        when(patientRepo.findById(1L)).thenReturn(Optional.of(patient));

        var ex = assertThrows(StatusConflictException.class,
                () -> appointmentService.createAppointment(req));

        assertEquals("Cannot create appointment for a discharged patient", ex.getMessage());
        verify(appointmentRepo, never()).save(any());
    }

   @Test
    void shouldUpdateAppointmentBasicFieldsSuccessfully() {
        Appointment existing = new Appointment();
        existing.setId(1L);
        existing.setAppDateTime(LocalDateTime.of(2026, 11, 2, 13, 0));
        existing.setPrice(BigDecimal.valueOf(30000));
        existing.setType(AppointmentType.GENERAL);

        AppointmentUpdateRequest updateRequest = new AppointmentUpdateRequest(
                LocalDateTime.of(2026, 11, 2, 13, 0),
                BigDecimal.valueOf(20000),
                null, null, null,
                null,
                AppointmentType.GENERAL
        );

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepo.existsByAppDateTimeAndIdNotAndStatusNot(
                updateRequest.appDateTime(), 1L, AppointmentStatus.CANCELLED)).thenReturn(false);
        when(appointmentRepo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.updateAppointment(1L, updateRequest);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(updateRequest.appDateTime(), response.appDateTime());
        assertEquals(updateRequest.price(), response.price());
        assertEquals(AppointmentType.GENERAL, response.type());
        verify(appointmentRepo).save(existing);
    }

    @Test
    void shouldThrowExceptionIfUpdateSlotIsAlreadyBooked() {
        Appointment existing = new Appointment();
        existing.setId(1L);
        existing.setAppDateTime(LocalDateTime.of(2026, 11, 2, 13, 0));
        existing.setPrice(BigDecimal.valueOf(30000));
        existing.setType(AppointmentType.GENERAL);

        AppointmentUpdateRequest updateRequest = new AppointmentUpdateRequest(
                LocalDateTime.of(2026, 11, 2, 13, 40),
                BigDecimal.valueOf(30000),
                null, null, null,
                null,
                AppointmentType.GENERAL
        );

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepo.existsByAppDateTimeAndIdNotAndStatusNot(
                updateRequest.appDateTime(), 1L, AppointmentStatus.CANCELLED)).thenReturn(true);

       var ex =  assertThrows(StatusConflictException.class, () -> appointmentService.updateAppointment(1L, updateRequest));
       assertEquals("This time slot is already booked", ex.getMessage());
       verify(appointmentRepo, never()).save(any());
    }

    @Test
    void shouldThrowExceptionIfUpdatePatientIdNotFound() {
        Appointment existing = new Appointment();
        existing.setId(1L);

        AppointmentUpdateRequest updateRequest = new AppointmentUpdateRequest(
                LocalDateTime.of(2024, 6, 2, 13, 0),
                BigDecimal.valueOf(30000),
                null, null, null,
                99L,
                AppointmentType.GENERAL
        );

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepo.existsByAppDateTimeAndIdNotAndStatusNot(
                updateRequest.appDateTime(), 1L, AppointmentStatus.CANCELLED)).thenReturn(false);
        when(patientRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.updateAppointment(1L, updateRequest));
        verify(appointmentRepo, never()).save(any());
    }
    @Test
    void shouldThrowIfMixedPatientDataOnUpdate() {
        Appointment existing = new Appointment();
        existing.setId(1L);

        AppointmentUpdateRequest req = new AppointmentUpdateRequest(
                null, null,
                "Ana", null, null,
                1L,
                null
        );

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(existing));

        var ex = assertThrows(StatusConflictException.class,
                () -> appointmentService.updateAppointment(1L, req));
        assertEquals("Provide either patientId or patient data, not both", ex.getMessage());
    }
    @Test
    void shouldUnlinkPatientWhenUpdatingManualData() {
        Appointment existing = new Appointment();
        existing.setId(1L);

        Patient patient = new Patient();
        patient.setId(1L);
        existing.setPatient(patient);

        AppointmentUpdateRequest req = new AppointmentUpdateRequest(
                null, null,
                "Ana", "Gomez", "12345678",
                null,
                null
        );

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        AppointmentResponse res = appointmentService.updateAppointment(1L, req);

        verify(appointmentRepo).save(captor.capture());
        Appointment saved = captor.getValue();

        assertNull(saved.getPatient());
        assertEquals("Ana", saved.getPatientFirstName());
        assertEquals("Gomez", saved.getPatientLastName());
        assertEquals("12345678", saved.getPatientPhone());
        assertEquals(1L, res.id());
        assertEquals("Ana Gomez", res.patientFullName());
        assertEquals("12345678", res.patientPhone());
    }

    @Test
    void shouldAssignNewPatientOnUpdate() {
        Appointment existing = new Appointment();
        existing.setId(1L);

        Patient patient = new Patient();
        patient.setId(2L);
        patient.setFirstname("Carlos");
        patient.setLastname("Lopez");
        patient.setStatus(PatientStatus.ACTIVE);

        AppointmentUpdateRequest req = new AppointmentUpdateRequest(
                null, null,
                null, null, null,
                2L,
                null
        );

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(patientRepo.findById(2L)).thenReturn(Optional.of(patient));
        when(appointmentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        AppointmentResponse res = appointmentService.updateAppointment(1L, req);

        assertEquals(1L, res.id());
        assertEquals("Carlos Lopez", res.patientFullName());
    }


    @Test
    void shouldCancelAppointmentSuccessfully() {
        Appointment existing = new Appointment();
        existing.setId(1L);
        existing.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.cancelAppointment(1L);

        assertNotNull(response);
        assertEquals(AppointmentStatus.CANCELLED, response.status());
        verify(appointmentRepo).save(existing);
    }

    @Test
    void shouldThrowIfAlreadyCancelled() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(a));

        var ex = assertThrows(StatusConflictException.class,
                () -> appointmentService.cancelAppointment(1L));
        assertEquals("Appointment is already cancelled", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCancelAppointmentNotFound() {
        when(appointmentRepo.findById(1L)).thenReturn(Optional.empty());
        var ex = assertThrows(ResourceNotFoundException.class, () -> appointmentService.cancelAppointment(1L));
        assertEquals("Appointment not found with id: 1", ex.getMessage());
    }


    @Test
    void shouldMarkAsAttendedSuccessfully() {
        Appointment existing = new Appointment();
        existing.setId(1L);
        existing.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.markAsAttended(1L);

        assertNotNull(response);
        assertEquals(AppointmentStatus.ATTENDED, response.status());
        verify(appointmentRepo).save(existing);
    }

    @Test
    void shouldThrowExceptionWhenMarkAsAttendedNotFound() {
        when(appointmentRepo.findById(1L)).thenReturn(Optional.empty());
        var ex = assertThrows(ResourceNotFoundException.class, () -> appointmentService.markAsAttended(1L));
        assertEquals("Appointment not found with id: 1", ex.getMessage());
    }

    @Test
    void shouldThrowIfAttendedAlready() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.ATTENDED);

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(a));

        var ex = assertThrows(StatusConflictException.class,
                () -> appointmentService.markAsAttended(1L));
        assertEquals("Appointment is already marked as attended", ex.getMessage());
    }

    @Test
    void shouldThrowIfAttendedCancelled() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(a));

        var ex = assertThrows(StatusConflictException.class,
                () -> appointmentService.markAsAttended(1L));
        assertEquals("Cannot mark a cancelled appointment as attended", ex.getMessage());
    }

    @Test
    void shouldMarkAsNoShowSuccessfully() {
        Appointment existing = new Appointment();
        existing.setId(1L);
        existing.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.markAsNoShow(1L);

        assertNotNull(response);
        assertEquals(AppointmentStatus.NO_SHOW, response.status());
        verify(appointmentRepo).save(existing);
    }

    @Test
    void shouldThrowIfNoShowAlready() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.NO_SHOW);

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(a));

        var ex = assertThrows(StatusConflictException.class,
                () -> appointmentService.markAsNoShow(1L));
        assertEquals("Appointment is already marked as no-show", ex.getMessage());
    }

    @Test
    void shouldThrowIfNoShowCancelled() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(a));

        var ex = assertThrows(StatusConflictException.class,
                () -> appointmentService.markAsNoShow(1L));
        assertEquals("Cannot mark a cancelled appointment as no-show", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenMarkAsNoShowNotFound() {
        when(appointmentRepo.findById(1L)).thenReturn(Optional.empty());
        var ex = assertThrows(ResourceNotFoundException.class, () -> appointmentService.markAsNoShow(1L));
        assertEquals("Appointment not found with id: 1", ex.getMessage());
    }
}
