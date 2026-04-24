package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.authDTO.AuthResponse;
import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.dto.authDTO.MessageResponse;
import com.camicompany.PsyCare.dto.authDTO.UpdatePassword;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.model.UserApp;
import com.camicompany.PsyCare.repository.UserAppRepository;
import com.camicompany.PsyCare.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImp implements AuthService {

    private final UserAppRepository userAppRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImp(UserAppRepository userAppRepo, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.userAppRepo = userAppRepo;
        this.passwordEncoder = passwordEncoder;

        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }


    @Override
    public AuthResponse loginUser(LoginRequest loginUser) {
        String username = loginUser.username();
        String password = loginUser.password();

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.generateToken(authentication);

        return new AuthResponse(accessToken);
    }

    @Override
    public MessageResponse updatePassword(UpdatePassword request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserApp userApp = userAppRepo.findByUsername(username).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        if(!passwordEncoder.matches(request.oldPassword(), userApp.getPassword())){
            throw new BadCredentialsException("Current password is incorrect");
        }
        userApp.setPassword(encryptPassword(request.newPassword()));
        userAppRepo.save(userApp);

        return new MessageResponse("Password updated successfully");
    }

    @Override
    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }


}
