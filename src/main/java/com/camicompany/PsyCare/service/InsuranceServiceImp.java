package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceCreateRequest;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceResponse;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.exception.StatusConflictException;
import com.camicompany.PsyCare.mapper.InsuranceMapper;
import com.camicompany.PsyCare.model.Insurance;
import com.camicompany.PsyCare.repository.InsuranceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InsuranceServiceImp implements InsuranceService{

    private final InsuranceRepository insuranceRepo;
    private final InsuranceMapper insuranceMapper;

    public InsuranceServiceImp(InsuranceRepository insuranceRepository, InsuranceMapper insuranceMapper) {
        this.insuranceRepo = insuranceRepository;
        this.insuranceMapper = insuranceMapper;
    }

    @Override
    public InsuranceResponse createInsurance(InsuranceCreateRequest insurance) {
        if(insuranceRepo.findByName(insurance.name()).isPresent()) {
            throw new StatusConflictException("Insurance with name " + insurance.name() + " already exists");
        }

        String cuit = normalizeCuit(insurance.cuit());
        if(insuranceRepo.existsByCuit(cuit)) {
            throw new StatusConflictException("Insurance with CUIT " + insurance.cuit() + " already exists");
        }
        Insurance insuranceEntity = insuranceMapper.toEntity(insurance);
        insuranceEntity.setCuit(cuit);
        return insuranceMapper.toResponse(insuranceRepo.save(insuranceEntity));
    }

    @Override
    public InsuranceResponse updateInsurance(Long id, InsuranceUpdateRequest insurance) {
        Insurance insuranceToUpdate = insuranceRepo.findById(id).orElseThrow( ()-> new ResourceNotFoundException("Insurance not found with id " + id));
        if(insurance.cuit() != null) {
            String cuit = normalizeCuit(insurance.cuit());
            if(insuranceRepo.existsByCuitAndIdNot(cuit, id)) {
                throw new StatusConflictException("Insurance with CUIT " + insurance.cuit() + " already exists");
            }
            insuranceToUpdate.setCuit(cuit);
        }

        if(insurance.name() != null){
            if(insuranceRepo.existsByNameAndIdNot(insurance.name(), id)){
                throw  new StatusConflictException("Insurance with name " + insurance.name() + " already exists");
            }
            insuranceToUpdate.setName(insurance.name());
        }

        Insurance updatedInsurance = insuranceRepo.save(insuranceToUpdate);
        return insuranceMapper.toResponse(updatedInsurance);
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceResponse getInsuranceById(Long id) {
            Insurance insurance = insuranceRepo.findById(id).orElseThrow( ()-> new ResourceNotFoundException("Insurance not found with id " + id));
            return insuranceMapper.toResponse(insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceResponse getInsuranceByName(String name) {
        Insurance insurance = insuranceRepo.findByName(name).orElseThrow( ()-> new ResourceNotFoundException("Insurance not found with name " + name));
        return insuranceMapper.toResponse(insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceResponse> getAllInsurances() {
        return insuranceRepo.findAll()
                .stream()
                .map(insuranceMapper::toResponse)
                .toList();
    }

    private String normalizeCuit(String cuit) {
        return cuit.replaceAll("[^\\d]", "");
    }
}
