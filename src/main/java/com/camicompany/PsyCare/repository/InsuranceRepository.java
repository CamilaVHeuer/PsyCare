package com.camicompany.PsyCare.repository;

import com.camicompany.PsyCare.model.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, Long> {

    Optional<Insurance> findByName(String name);

    boolean existsByCuit(String cuit);

    boolean existsByCuitAndIdNot(String cuit, Long id);

    boolean existsByNameAndIdNot(String name, Long id);
}
