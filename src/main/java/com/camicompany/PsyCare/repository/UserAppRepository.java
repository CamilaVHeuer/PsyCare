package com.camicompany.PsyCare.repository;

import com.camicompany.PsyCare.model.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAppRepository extends JpaRepository<UserApp, Long> {
    public Optional<UserApp> findByUsername(String username);

    public boolean existsByUsername( String username);
}
