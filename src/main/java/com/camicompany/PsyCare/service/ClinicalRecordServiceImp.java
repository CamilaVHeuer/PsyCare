package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordCreateRequest;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordResponse;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.exception.StatusConflictException;
import com.camicompany.PsyCare.mapper.ClinicalRecordMapper;
import com.camicompany.PsyCare.model.ClinicalRecord;
import com.camicompany.PsyCare.model.Patient;
import com.camicompany.PsyCare.repository.ClinicalRecordRepository;
import com.camicompany.PsyCare.repository.PatientRepository;
import org.springframework.stereotype.Service;

@Service
public class ClinicalRecordServiceImp implements ClinicalRecordService {
    private final ClinicalRecordRepository clinicalRecordRepo;
    private final ClinicalRecordMapper clinicalRecordMapper;
    private final PatientRepository patientRepo;

    public ClinicalRecordServiceImp(ClinicalRecordRepository clinicalRecordRepo, ClinicalRecordMapper clinicalRecordMapper, PatientRepository patientRepo) {
        this.clinicalRecordRepo = clinicalRecordRepo;
        this.clinicalRecordMapper = clinicalRecordMapper;
        this.patientRepo = patientRepo;
    }

    @Override
    public ClinicalRecordResponse getClinicalRecordById(Long id) {
        ClinicalRecord clinicalRecord = clinicalRecordRepo.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("ClinicalRecord not found with id: " + id));
        return clinicalRecordMapper.toResponse(clinicalRecord);
    }

    @Override
    public ClinicalRecordResponse createClinicalRecord(Long patientId, ClinicalRecordCreateRequest clinicalRecord) {

        Patient patient = patientRepo.findById(patientId).orElseThrow(
                ()-> new ResourceNotFoundException("Patient not found with id: " + patientId));
        if(patient.getClinicalRecord() != null){
            throw new StatusConflictException("Patient with id " + patientId + " already has a clinical record.");
        }

        ClinicalRecord clinicalRecordEntity = clinicalRecordMapper.toEntity(clinicalRecord);

        // Set bidirectional relationship
        clinicalRecordEntity.setPatient(patient);
        patient.setClinicalRecord(clinicalRecordEntity);

        // Save the clinical record from owner side to ensure the relationship is persisted
        patientRepo.save(patient);

        return clinicalRecordMapper.toResponse(patient.getClinicalRecord());
    }

    @Override
    public ClinicalRecordResponse updateClinicalRecord(Long id, ClinicalRecordUpdateRequest clinicalRecord) {

        ClinicalRecord clinicalRecordEntity = clinicalRecordRepo.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("ClinicalRecord not found with id: " + id));

        if(clinicalRecord.reasonConsult() != null){
            clinicalRecordEntity.setReasonConsult(clinicalRecord.reasonConsult());
        }
        if(clinicalRecord.diagnosis() != null){
            clinicalRecordEntity.setDiagnosis(clinicalRecord.diagnosis());
        }
        if(clinicalRecord.obs() != null){
            clinicalRecordEntity.setObs(clinicalRecord.obs());
        }
        if(clinicalRecord.medication() != null){
            clinicalRecordEntity.setMedication(clinicalRecord.medication());
        }
        ClinicalRecord updatedClinicalRecord = clinicalRecordRepo.save(clinicalRecordEntity);
        return clinicalRecordMapper.toResponse(updatedClinicalRecord);
    }
}
