package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.model.Appointment;
import com.camicompany.PsyCare.model.AppointmentStatus;
import com.camicompany.PsyCare.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AgendaServiceImp implements AgendaService {

    private final AppointmentRepository appointmentRepo;
    private final int startHour;
    private final int endHour;
    private final int sessionDuration;

    public AgendaServiceImp(AppointmentRepository appointmentRepo,
                            @Value("${agenda.start.hour}") int startHour,
                            @Value("${agenda.end.hour}") int endHour,
                            @Value("${agenda.session.duration}") int sessionDuration) {
        this.appointmentRepo = appointmentRepo;
        this.startHour = startHour;
        this.endHour = endHour;
        this.sessionDuration = sessionDuration;
    }

    @Override
    public List<LocalDateTime> getAvailableSlots(LocalDate date) {
        validateWorkingDay(date);

        List<LocalDateTime> allSlots = generateDailySlots(date);
        List<LocalDateTime> bookedSlots = getBookedSlots(date);

        return allSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .toList();
    }

    @Override
    public boolean isSlotAvailable(LocalDateTime dateTime) {
        validateWorkingDay(dateTime.toLocalDate());
        validateWorkingHour(dateTime);
        return !appointmentRepo.existsByAppDateTimeAndStatusNot(
                dateTime,
                AppointmentStatus.CANCELLED);
    }

    // Helper methods
    private void validateWorkingDay(LocalDate date) {
        if(date.getDayOfWeek() == DayOfWeek.SATURDAY ||
        date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("No availability on weekends");
        }
    }

    private void validateWorkingHour(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        if(hour < startHour || hour >= endHour) {
            throw new IllegalArgumentException("Time outside working hours");
        }
    }

    private List<LocalDateTime> generateDailySlots(LocalDate date) {
        List<LocalDateTime> slots = new ArrayList<>();
        LocalDateTime current = date.atTime(startHour, 0);
        LocalDateTime end = date.atTime(endHour, 0);

        while (current.plusMinutes(sessionDuration).isBefore(end) ||
                current.plusMinutes(sessionDuration).equals(end)) {
            slots.add(current);
            current = current.plusMinutes(sessionDuration);
        }
        return slots;
    }

    private List<LocalDateTime> getBookedSlots(LocalDate date) {
    LocalDateTime start = date.atTime(startHour, 0);
    LocalDateTime end = date.atTime(endHour, 0);

        List<Appointment> appointments = appointmentRepo.findByAppDateTimeBetweenAndStatusNot(
                start, end, AppointmentStatus.CANCELLED);

        return appointments.stream()
                .map(Appointment::getAppDateTime)
                .toList();
    }


}
