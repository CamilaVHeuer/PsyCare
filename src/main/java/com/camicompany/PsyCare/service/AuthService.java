package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.authDTO.AuthResponse;
import com.camicompany.PsyCare.dto.authDTO.LoginRequest;
import com.camicompany.PsyCare.dto.authDTO.MessageResponse;
import com.camicompany.PsyCare.dto.authDTO.UpdatePassword;

public interface AuthService {
    //Login User
    public AuthResponse loginUser(LoginRequest loginUser);

    //Update profile (only password)
    public MessageResponse updatePassword(UpdatePassword request);

    //Encrypt password
    public String encryptPassword(String password);
}
