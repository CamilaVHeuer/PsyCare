package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorResponse;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRelationRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.exception.StatusConflictException;
import com.camicompany.PsyCare.mapper.TutorMapper;
import com.camicompany.PsyCare.model.Tutor;
import com.camicompany.PsyCare.repository.TutorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TutorServiceImp implements TutorService {

    private final TutorRepository tutorRepo;
    private final TutorMapper tutorMapper;

    public TutorServiceImp(TutorRepository tutorRepository, TutorMapper tutorMapper) {
        this.tutorRepo = tutorRepository;
        this.tutorMapper = tutorMapper;
    }

    @Override
    public TutorResponse createTutor(TutorCreateRequest tutor) {
            String normalizedCuil = normalizeCuil(tutor.cuil());

            if(tutorRepo.existsByCuil(normalizedCuil)) {
                throw new StatusConflictException("The CUIL " + tutor.cuil() + " is already registered for another tutor");
            }

            Tutor tutorEntity = tutorMapper.toEntity(tutor);
            tutorEntity.setCuil(normalizedCuil);
            Tutor savedTutor = tutorRepo.save(tutorEntity);
            return tutorMapper.toResponse(savedTutor);
    }

    @Override
    public TutorResponse updateTutor(Long id, TutorUpdateRequest tutor) {

        Tutor tutorExisting = tutorRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + id));
        if (tutor.cuil()!= null){
            String normalizedCuil = normalizeCuil(tutor.cuil());
            if(tutorRepo.existsByCuilAndIdNot(normalizedCuil, id)) {
                throw new StatusConflictException("The CUIL " + tutor.cuil() + " is already registered for another tutor");
            }
            tutorExisting.setCuil(normalizedCuil);
        }

        if (tutor.firstname()!= null){
            tutorExisting.setFirstname(tutor.firstname());
        }
        if (tutor.lastname()!= null){
            tutorExisting.setLastname(tutor.lastname());
        }
        if (tutor.phone()!= null){
            tutorExisting.setPhone(tutor.phone());
        }

        tutorRepo.save(tutorExisting);
        return tutorMapper.toResponse(tutorExisting);
    }

    @Override
    public TutorResponse changeRelationTutorPatient(Long id, TutorUpdateRelationRequest relation) {
        if(relation == null){
            throw new IllegalArgumentException("Relation cannot be null");
        }
        Tutor tutorExisting = tutorRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + id));
        tutorExisting.setRelation(relation.relation());
        tutorRepo.save(tutorExisting);
        return tutorMapper.toResponse(tutorExisting);
    }

    public Tutor findOrCreateTutor(TutorCreateRequest tutorRequest) {
        String normalizedCuil = normalizeCuil(tutorRequest.cuil());

        return tutorRepo.findByCuil(normalizedCuil)
                .orElseGet(() -> {
                    Tutor tutor = tutorMapper.toEntity(tutorRequest);
                    tutor.setCuil(normalizedCuil);
                    return tutorRepo.save(tutor);
                });
    }

    //helper method

    private String normalizeCuil(String cuil) {
        return cuil.replaceAll("[^\\d]", "");
    }
}
