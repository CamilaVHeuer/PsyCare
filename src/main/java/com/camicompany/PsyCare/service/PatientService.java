package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.patientDTO.PatientCreateRequest;
import com.camicompany.PsyCare.dto.patientDTO.PatientResponse;
import com.camicompany.PsyCare.dto.patientDTO.PatientSummaryResponse;
import com.camicompany.PsyCare.dto.patientDTO.PatientUpdateRequest;

import java.util.List;

public interface PatientService {

    PatientResponse getPatientById(Long id);

    //esto en el futuro deberia paginarse o ver opciones de analisis de negocio: countar activos y dados de alta
    List<PatientSummaryResponse> getAllPatients();

    PatientResponse createPatient(PatientCreateRequest patient);

    PatientResponse updatePatient(Long id, PatientUpdateRequest patient);

    PatientResponse dischargePatient(Long id);

    PatientResponse reactivePatient(Long id);


}
