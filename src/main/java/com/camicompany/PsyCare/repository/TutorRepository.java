package com.camicompany.PsyCare.repository;

import com.camicompany.PsyCare.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TutorRepository extends JpaRepository<Tutor,Long> {

    boolean existsByCuil(String cuil);

    boolean existsByCuilAndIdNot(String cuil, Long id);

    Optional<Tutor> findByCuil(String cuil);
}
