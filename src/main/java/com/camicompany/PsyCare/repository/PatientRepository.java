package com.camicompany.PsyCare.repository;

import com.camicompany.PsyCare.model.Patient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByNationalId(String nationalId);
}
