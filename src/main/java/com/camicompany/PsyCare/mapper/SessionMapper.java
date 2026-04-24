package com.camicompany.PsyCare.mapper;

import com.camicompany.PsyCare.dto.sessionDTO.SessionCreateRequest;
import com.camicompany.PsyCare.dto.sessionDTO.SessionResponse;
import com.camicompany.PsyCare.model.Session;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class SessionMapper {

    public SessionResponse toResponse(Session session) {
        if (session == null) {
            return null;
        }

        return new SessionResponse(
            session.getId(),
            session.getSessionDate(),
            session.getEvolutionNotes());
    }

    public Session toEntity(SessionCreateRequest request) {
        if (request == null) {
            return null;
        }

        Session session = new Session();
        session.setSessionDate(request.sessionDate());
        session.setEvolutionNotes(request.evolutionNotes());

        return session;
    }

    public List<SessionResponse> toResponseList(List<Session> sessions) {
        if (sessions == null) {
            return List.of();
        }

        return sessions.stream()
            .map(this::toResponse)
            .toList();

    }
}
