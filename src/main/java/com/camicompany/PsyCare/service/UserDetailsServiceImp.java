package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.model.UserApp;
import com.camicompany.PsyCare.repository.UserAppRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImp implements UserDetailsService {

    private final UserAppRepository userAppRepo;

    public UserDetailsServiceImp(UserAppRepository userAppRepo) {
        this.userAppRepo = userAppRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserApp userApp = userAppRepo.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username));

        List<SimpleGrantedAuthority> authoritiesList= new ArrayList<>();
        authoritiesList.add(new SimpleGrantedAuthority("ROLE_".concat(userApp.getRole().getRoleName())));

        userApp.getRole().getPermissionsList()
                .forEach(permission -> authoritiesList.add(new SimpleGrantedAuthority(permission.getPermissionName())));

        return new User(
                userApp.getUsername(),
                userApp.getPassword(),
                userApp.isEnabled(),
                userApp.isAccountNotExpired(),
                userApp.isCredentialsNotExpired(),
                userApp.isAccountNotLocked(),
                authoritiesList
        );

    }
}
