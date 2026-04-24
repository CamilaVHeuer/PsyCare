package com.camicompany.PsyCare.repository;

import com.camicompany.PsyCare.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPayDateBetween(LocalDate start, LocalDate end);
}
