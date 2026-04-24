package com.camicompany.PsyCare.repository;

import com.camicompany.PsyCare.model.ClinicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicalRecordRepository extends JpaRepository<ClinicalRecord, Long> {
}
