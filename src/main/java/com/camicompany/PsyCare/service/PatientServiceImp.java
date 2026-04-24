package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.patientDTO.PatientCreateRequest;
import com.camicompany.PsyCare.dto.patientDTO.PatientResponse;
import com.camicompany.PsyCare.dto.patientDTO.PatientSummaryResponse;
import com.camicompany.PsyCare.dto.patientDTO.PatientUpdateRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.exception.*;
import com.camicompany.PsyCare.mapper.PatientMapper;

import com.camicompany.PsyCare.model.Insurance;
import com.camicompany.PsyCare.model.Patient;
import com.camicompany.PsyCare.model.PatientStatus;
import com.camicompany.PsyCare.model.Tutor;
import com.camicompany.PsyCare.repository.InsuranceRepository;
import com.camicompany.PsyCare.repository.PatientRepository;
import com.camicompany.PsyCare.repository.TutorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@Transactional
public class PatientServiceImp implements PatientService {

    private final PatientRepository patientRepo;
    private final PatientMapper patientMapper;
    private final TutorRepository tutorRepo;
    private final InsuranceRepository insuranceRepo;
    private final TutorService tutorService;


    public PatientServiceImp(PatientRepository patientRepo, PatientMapper patientMapper, TutorRepository tutorRepo, InsuranceRepository insuranceRepo, TutorService tutorService) {
        this.patientRepo = patientRepo;
        this.patientMapper = patientMapper;
        this.tutorRepo = tutorRepo;
        this.insuranceRepo = insuranceRepo;
        this.tutorService = tutorService;
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        //convert to response
        return patientMapper.toResponse(patient);

    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryResponse> getAllPatients() {
        return patientRepo.findAll().stream()
                .map(patientMapper::toResponseSummary)
                .toList();
    }

    @Override
    public PatientResponse createPatient(PatientCreateRequest patient) {
        //check if nationalId already exists
        validateDuplicateNationalId(patient.nationalId());
        //check tutor or tutorId but not both
        validateTutorInput(patient.tutorId(), patient.tutor());

        //check if minor (required tutor)
        validateMinorWithTutor(patient.birthDate(), patient.tutorId(), patient.tutor());

        //tutor exists, or I have tutor info to create new tutor
        Tutor tutor = resolveTutor(patient.tutorId(), patient.tutor());

        //check insurance if provided
        Insurance insurance = resolveInsurance(patient.insuranceId());
        //convert to entity
        Patient patientEntity = patientMapper.toEntity(patient);
        patientEntity.setTutor(tutor);
        patientEntity.setInsurance(insurance);
        patientEntity.setInsuranceNumber(patient.insuranceNumber());
        patientEntity.setStatus(PatientStatus.ACTIVE);
        //save to db
        Patient savedPatient = patientRepo.save(patientEntity);
        //convert to response
        return patientMapper.toResponse(savedPatient);
    }

    @Override
    public PatientResponse updatePatient(Long id, PatientUpdateRequest patient) {
        Patient existingPatient = findPatientOrThrow(id);

        updateBasicFields(existingPatient, patient);
        //check if nationalId is being updated to an existing one
        if (patient.nationalId() != null) {
            validateNationalIdUpdate(existingPatient, patient.nationalId());
            existingPatient.setNationalId(patient.nationalId());
        }

        validateTutorInput(patient.tutorId(), patient.tutor());

        // check if minor and tutor info is provided if birthDate is updated
        validateMinorUpdate(existingPatient, patient);

        Tutor tutor = resolveTutor(patient.tutorId(), patient.tutor());
        if(tutor!=null) {
            existingPatient.setTutor(tutor);
        }
        //check insurance if provided
        Insurance insurance = resolveInsurance(patient.insuranceId());
        if(insurance!=null) {
            existingPatient.setInsurance(insurance);
        }

        //save updated entity
        Patient updatedPatient = patientRepo.save(existingPatient);
        //convert to response
        return patientMapper.toResponse(updatedPatient);
    }

    @Override
    public PatientResponse dischargePatient(Long id) {
        Patient existingPatient = findPatientOrThrow(id);
       //check if already discharged
        if(PatientStatus.DISCHARGED.equals(existingPatient.getStatus())) {
            throw new StatusConflictException("Patient is already discharged");
        }
        existingPatient.setStatus(PatientStatus.DISCHARGED);
        Patient dischargedPatient = patientRepo.save(existingPatient);
        return patientMapper.toResponse(dischargedPatient);
    }

    @Override
    public PatientResponse reactivePatient(Long id) {
        Patient existingPatient = findPatientOrThrow(id);
        //check if already active
        if (PatientStatus.ACTIVE.equals(existingPatient.getStatus())) {
            throw new StatusConflictException("Patient is already active");
        }
        existingPatient.setStatus(PatientStatus.ACTIVE);
        Patient reactivePatient = patientRepo.save(existingPatient);
        return patientMapper.toResponse(reactivePatient);
    }

    //Helper methods
    private Patient findPatientOrThrow(Long id) {
        return patientRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    private void validateDuplicateNationalId(String nationalId) {
        if (patientRepo.existsByNationalId(nationalId)) {
            throw new DuplicateNationalIdException("Patient with this national ID already exists");
        }
    }

    private void validateNationalIdUpdate(Patient existing, String newNationalId) {
        if (!newNationalId.equals(existing.getNationalId()) &&
                patientRepo.existsByNationalId(newNationalId)) {
            throw new DuplicateNationalIdException("Patient with this national ID already exists");
        }
    }
    private void updateBasicFields(Patient patient, PatientUpdateRequest request) {
        if (request.firstname() != null) patient.setFirstname(request.firstname());
        if (request.lastname() != null) patient.setLastname(request.lastname());
        if (request.phone() != null) patient.setPhone(request.phone());
        if (request.insuranceNumber() != null) patient.setInsuranceNumber(request.insuranceNumber());
    }

    private void validateTutorInput(Long tutorId, TutorCreateRequest tutor) {
        if (tutorId != null && tutor != null) {
            throw new ConflictingTutorInformationException("Provide either tutorId or tutor info, not both");
        }
    }

    private void validateMinorWithTutor(LocalDate birthDate, Long tutorId, TutorCreateRequest tutor) {
        if (calculateAge(birthDate) < 18 && tutorId == null && tutor == null) {
            throw new MissingTutorException("Minor patients require tutor information");
        }
    }
    private void validateMinorUpdate(Patient existing, PatientUpdateRequest request) {
        if (request.birthDate() != null) {
            int age = calculateAge(request.birthDate());
            if (age < 18 &&
                    request.tutorId() == null &&
                    request.tutor() == null &&
                    existing.getTutor() == null) {
                throw new MissingTutorException("Minor patients require tutor information");
            }
        }
    }

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private Tutor resolveTutor(Long tutorId, TutorCreateRequest tutorRequest) {
        if (tutorId != null) {
            return tutorRepo.findById(tutorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + tutorId));
        }
        if (tutorRequest != null) {
            return tutorService.findOrCreateTutor(tutorRequest);
        }
        return null;
    }

    private Insurance resolveInsurance(Long insuranceId) {
        if (insuranceId == null) return null;

        return insuranceRepo.findById(insuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance not found with id: " + insuranceId));
    }
}
