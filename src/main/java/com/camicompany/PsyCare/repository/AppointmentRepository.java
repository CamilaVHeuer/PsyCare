package com.camicompany.PsyCare.repository;

import com.camicompany.PsyCare.model.Appointment;
import com.camicompany.PsyCare.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByAppDateTimeAndStatusNot(
            LocalDateTime dateTime,
            AppointmentStatus status
    );

    boolean existsByAppDateTimeAndIdNotAndStatusNot(
            LocalDateTime dateTime,
            Long id,
            AppointmentStatus status
    );

    List<Appointment> findByAppDateTimeBetweenAndStatusNot(
            LocalDateTime start,
            LocalDateTime end,
            AppointmentStatus appointmentStatus);
}
