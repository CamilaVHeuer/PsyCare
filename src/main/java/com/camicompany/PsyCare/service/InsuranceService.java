package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceCreateRequest;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceResponse;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceUpdateRequest;

import java.util.List;

public interface InsuranceService {

    public InsuranceResponse createInsurance(InsuranceCreateRequest insurance);

    public InsuranceResponse updateInsurance(Long id, InsuranceUpdateRequest insurance);

    public InsuranceResponse getInsuranceById(Long id);

    public InsuranceResponse getInsuranceByName(String name);

    public List<InsuranceResponse> getAllInsurances();

}
