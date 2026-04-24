package com.camicompany.PsyCare.config;

import com.camicompany.PsyCare.model.Permission;
import com.camicompany.PsyCare.model.Role;
import com.camicompany.PsyCare.model.UserApp;
import com.camicompany.PsyCare.repository.PermissionRepository;
import com.camicompany.PsyCare.repository.RoleRepository;
import com.camicompany.PsyCare.repository.UserAppRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Component
public class DataInitializer {
    @Value("${app.username}")
    private String username;

    @Value("${app.password}")
    private String password;

    @Bean
    CommandLineRunner initData(
            UserAppRepository userRepo,
            RoleRepository roleRepo,
            PermissionRepository permRepo,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            //Permissions
            createPermissionIfNotExists(permRepo, "CREATE");
            createPermissionIfNotExists(permRepo, "READ");
            createPermissionIfNotExists(permRepo, "UPDATE");
            createPermissionIfNotExists(permRepo, "DELETE");

            Permission create = permRepo.findByPermissionName("CREATE").orElseThrow();
            Permission read = permRepo.findByPermissionName("READ").orElseThrow();
            Permission update = permRepo.findByPermissionName("UPDATE").orElseThrow();
            Permission delete = permRepo.findByPermissionName("DELETE").orElseThrow();

            // ROLES
            createRoleIfNotExists(roleRepo, "ADMIN", Set.of(create, read, update, delete));
            createRoleIfNotExists(roleRepo, "USER", Set.of(read, update, create));

            // Create  ownerUser if not exist
            if (!userRepo.existsByUsername(username)) {

                Role userOwnerRole = roleRepo.findByRoleName("ADMIN").orElseThrow();

                UserApp ownerUser = new UserApp();
                ownerUser.setUsername(username);
                ownerUser.setPassword(
                        passwordEncoder.encode(password)
                );
                ownerUser.setEnabled(true);
                ownerUser.setAccountNotExpired(true);
                ownerUser.setCredentialsNotExpired(true);
                ownerUser.setAccountNotLocked(true);
                ownerUser.setRole(userOwnerRole);

                userRepo.save(ownerUser);
                System.out.println("✅ Usuario ADMIN creado correctamente");

            }
        };
    }

    //Helper method to create permissions
    private void createPermissionIfNotExists(
            PermissionRepository permRepo,
            String name) {
        if (!permRepo.existsByPermissionName(name)) {
            permRepo.save(new Permission(null, name));
        }
    }

    //Helper method to create roles
    private void createRoleIfNotExists(
            RoleRepository roleRepo,
            String roleName,
            Set<Permission> permissionsList) {
        if (!roleRepo.existsByRoleName(roleName)) {
            Role role = new Role();
            role.setRoleName(roleName);
            role.setPermissionsList(permissionsList);
            roleRepo.save(role);
        }
    }
}
