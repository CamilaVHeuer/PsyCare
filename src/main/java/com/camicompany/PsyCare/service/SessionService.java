package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.sessionDTO.SessionCreateRequest;
import com.camicompany.PsyCare.dto.sessionDTO.SessionResponse;
import com.camicompany.PsyCare.dto.sessionDTO.SessionUpdateRequest;

public interface SessionService {

    public SessionResponse getSessionById(Long id);

    public SessionResponse createSession(Long clinicalRecordId, SessionCreateRequest session);

    public SessionResponse updateSession(Long id, SessionUpdateRequest session);

}
