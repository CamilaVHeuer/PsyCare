package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.model.Appointment;
import com.camicompany.PsyCare.model.AppointmentStatus;
import com.camicompany.PsyCare.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class AgendaServiceImpTest {

    @Mock private AppointmentRepository appointmentRepo;

    private AgendaServiceImp agendaService;

    @BeforeEach
    public void setUp() {
        agendaService = new AgendaServiceImp(appointmentRepo, 13, 22, 40);
    }

    @Test
    void getAvailableSlotsShouldReturnAllSlotsWhenNoAppointments() {
    LocalDate date = LocalDate.of(2026, 11, 2); // Monday
        when(appointmentRepo.findByAppDateTimeBetweenAndStatusNot(
                any(), any(), eq(AppointmentStatus.CANCELLED)
        )).thenReturn(List.of());

        List<LocalDateTime> slots = agendaService.getAvailableSlots(date);

        // Horatio: 13:00 a 22:00, sessions of 40 minutes
        // Slots: 13:00, 13:40, 14:20, ..., 21:00
        assertFalse(slots.isEmpty());
        assertEquals(13, slots.size());
    assertEquals(LocalDateTime.of(2026, 11, 2, 13, 0), slots.get(0));
    assertEquals(LocalDateTime.of(2026, 11, 2, 21, 0), slots.get(slots.size() - 1));
    }

    @Test
    void getAvailableSlotsShouldExcludeBookedSlots() {
    LocalDate date = LocalDate.of(2026, 11, 2); // Monday
    Appointment booked = new Appointment();
    booked.setAppDateTime(LocalDateTime.of(2026, 11, 2, 15, 40));
        when(appointmentRepo.findByAppDateTimeBetweenAndStatusNot(
                any(), any(), eq(AppointmentStatus.CANCELLED)
        )).thenReturn(List.of(booked));

        List<LocalDateTime> slots = agendaService.getAvailableSlots(date);

    assertFalse(slots.contains(LocalDateTime.of(2026, 11, 2, 15, 40)));
        assertEquals(12, slots.size());
    }

    @Test
    void getAvailableSlotsShouldThrowIfWeekend() {
    LocalDate saturday = LocalDate.of(2026, 11, 7); // Saturday
    LocalDate sunday = LocalDate.of(2026, 11, 8);   // Sunday

        var exSat = assertThrows(IllegalArgumentException.class, () -> agendaService.getAvailableSlots(saturday));
        assertEquals("No availability on weekends", exSat.getMessage());
        var exSun = assertThrows(IllegalArgumentException.class, () -> agendaService.getAvailableSlots(sunday));
        assertEquals("No availability on weekends", exSun.getMessage());
    }

    @Test
    void isSlotAvailableShouldReturnTrueIfSlotFree() {
    LocalDateTime slot = LocalDateTime.of(2026, 11, 2, 14, 20); // Monday, working hour
        when(appointmentRepo.existsByAppDateTimeAndStatusNot(
                eq(slot), eq(AppointmentStatus.CANCELLED)
        )).thenReturn(false);

        boolean available = agendaService.isSlotAvailable(slot);
        assertTrue(available);
    }

    @Test
    void isSlotAvailableShouldReturnFalseIfSlotBooked() {
    LocalDateTime slot = LocalDateTime.of(2026, 11, 2, 14, 20); // Monday, working hour
        when(appointmentRepo.existsByAppDateTimeAndStatusNot(
                eq(slot), eq(AppointmentStatus.CANCELLED)
        )).thenReturn(true);

        boolean available = agendaService.isSlotAvailable(slot);
        assertFalse(available);
    }

    @Test
    void isSlotAvailableShouldThrowIfWeekend() {
    LocalDateTime saturday = LocalDate.of(2026, 11, 7).atTime(14, 20); // Saturday
        var ex = assertThrows(IllegalArgumentException.class, () -> agendaService.isSlotAvailable(saturday));
        assertEquals("No availability on weekends", ex.getMessage());
    }

    @Test
    void isSlotAvailableShouldThrowIfOutsideWorkingHours() {
    LocalDateTime early = LocalDate.of(2026, 11, 2).atTime(12, 0); // Before 13:00
    LocalDateTime late = LocalDate.of(2026, 11, 2).atTime(22, 0);  // After 21:20

        var exEarly = assertThrows(IllegalArgumentException.class, () -> agendaService.isSlotAvailable(early));
        assertEquals("Time outside working hours", exEarly.getMessage());

        var exLate = assertThrows(IllegalArgumentException.class, () -> agendaService.isSlotAvailable(late));
        assertEquals("Time outside working hours", exLate.getMessage());
    }
    @Test
    void lastSlotShouldNotExceedEndHour() {
        LocalDate date = LocalDate.of(2026, 11, 2);

        when(appointmentRepo.findByAppDateTimeBetweenAndStatusNot(any(), any(), any()))
                .thenReturn(List.of());

        List<LocalDateTime> slots = agendaService.getAvailableSlots(date);

        LocalDateTime last = slots.get(slots.size() - 1);

        assertTrue(last.plusMinutes(40).getHour() <= 22);
    }
}
