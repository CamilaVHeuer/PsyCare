package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordCreateRequest;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordResponse;
import com.camicompany.PsyCare.dto.clinicalRecordtDTO.ClinicalRecordUpdateRequest;

public interface ClinicalRecordService {

    public ClinicalRecordResponse getClinicalRecordById(Long id);

    public ClinicalRecordResponse createClinicalRecord(Long patientId, ClinicalRecordCreateRequest clinicalRecord);

    public ClinicalRecordResponse updateClinicalRecord(Long id, ClinicalRecordUpdateRequest clinicalRecord);
}
