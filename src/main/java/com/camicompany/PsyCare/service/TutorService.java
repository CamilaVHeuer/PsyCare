package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorResponse;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRelationRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRequest;
import com.camicompany.PsyCare.model.Tutor;

public interface TutorService {

    //public TutorResponse getTutorById(Long id);

    public TutorResponse createTutor(TutorCreateRequest tutor);

    public TutorResponse updateTutor(Long id, TutorUpdateRequest tutor);

    public TutorResponse changeRelationTutorPatient(Long id, TutorUpdateRelationRequest relation);

    public Tutor findOrCreateTutor(TutorCreateRequest tutorRequest);
}
