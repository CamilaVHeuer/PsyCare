package com.camicompany.PsyCare.mapper;

import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordCreateRequest;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordResponse;
import com.camicompany.PsyCare.model.ClinicalRecord;
import org.springframework.stereotype.Component;

@Component
public class ClinicalRecordMapper {

    private final SessionMapper sessionMapper;

    public ClinicalRecordMapper(SessionMapper sessionMapper) {
        this.sessionMapper = sessionMapper;
    }

    public ClinicalRecordResponse toResponse(ClinicalRecord clinicalRecord) {
        if (clinicalRecord == null) {
            return null;
        }

        return new ClinicalRecordResponse(
            clinicalRecord.getId(),
            clinicalRecord.getPatient().getFirstname(),
            clinicalRecord.getPatient().getLastname(),
            clinicalRecord.getDiagnosis(),
            clinicalRecord.getObs(),
            clinicalRecord.getMedication(),
            sessionMapper.toResponseList(clinicalRecord.getSessions())
        );
    }

    public ClinicalRecord toEntity(ClinicalRecordCreateRequest request) {
        if (request == null) {
            return null;
        }

        ClinicalRecord clinicalRecord = new ClinicalRecord();
        clinicalRecord.setReasonConsult(request.reasonConsult());
        clinicalRecord.setDiagnosis(request.diagnosis());
        clinicalRecord.setObs(request.obs());
        clinicalRecord.setMedication(request.medication());

        return clinicalRecord;
    }
}
