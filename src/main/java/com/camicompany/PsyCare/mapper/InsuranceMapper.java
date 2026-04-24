package com.camicompany.PsyCare.mapper;

import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceCreateRequest;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceResponse;
import com.camicompany.PsyCare.model.Insurance;
import org.springframework.stereotype.Component;

@Component
public class InsuranceMapper {

    public InsuranceResponse toResponse(Insurance insurance) {
        if (insurance == null) {
            return null;
        }
        return new InsuranceResponse(
                insurance.getId(),
                insurance.getName(),
                formatCuit(insurance.getCuit())
        );
    }

    public Insurance toEntity(InsuranceCreateRequest request) {
        if (request == null) {
            return null;
        }
        Insurance insurance = new Insurance();
        insurance.setName(request.name());
        insurance.setCuit(request.cuit());
        return insurance;
    }
    private String formatCuit(String cuit) {
        if (cuit == null || cuit.length() != 11) return cuit;

        return cuit.substring(0,2) + "-" +
                cuit.substring(2,10) + "-" +
                cuit.substring(10);
    }
}
