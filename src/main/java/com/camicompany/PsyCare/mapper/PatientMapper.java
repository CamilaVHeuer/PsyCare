package com.camicompany.PsyCare.mapper;

import com.camicompany.PsyCare.dto.patientDTO.PatientCreateRequest;
import com.camicompany.PsyCare.dto.patientDTO.PatientResponse;
import com.camicompany.PsyCare.dto.patientDTO.PatientSummaryResponse;
import com.camicompany.PsyCare.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    private final TutorMapper tutorMapper;

    public PatientMapper(TutorMapper tutorMapper) {
        this.tutorMapper = tutorMapper;
    }

    public PatientResponse toResponse(Patient patient) {
            if ( patient == null ) {
                return null;
            }

            return new PatientResponse(
            patient.getId(),
            patient.getFirstname(),
            patient.getLastname(),
            patient.getNationalId(),
            patient.getBirthDate(),
            patient.getAge(),
            patient.getPhone(),
            patient.getStatus(),
            tutorMapper.toResponse(patient.getTutor()),
            patient.getInsurance() != null ? patient.getInsurance().getName() : null,
            patient.getInsuranceNumber()
            );

        }


        public PatientSummaryResponse toResponseSummary(Patient patient) {
            if ( patient == null ) {
                return null;
            }
            return new PatientSummaryResponse(
                    patient.getId(),
                    patient.getFirstname(),
                    patient.getLastname(),
                    patient.getAge(),
                    patient.getInsurance() != null ? patient.getInsurance().getName() : null
            );
        }

        public Patient toEntity(PatientCreateRequest request) {
            if (request == null ) {
                return null;
            }

            Patient patient = new Patient();

            patient.setFirstname(request.firstname());
            patient.setLastname(request.lastname());
            patient.setNationalId(request.nationalId());
            patient.setBirthDate(request.birthDate() );
            patient.setPhone(request.phone());

            return patient;
        }




    }




