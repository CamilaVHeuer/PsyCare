package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceCreateRequest;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceResponse;
import com.camicompany.PsyCare.dto.insuranceDTO.InsuranceUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.exception.StatusConflictException;
import com.camicompany.PsyCare.mapper.InsuranceMapper;
import com.camicompany.PsyCare.model.Insurance;
import com.camicompany.PsyCare.repository.InsuranceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InsuranceServiceImpTest {

    @Mock private InsuranceRepository insuranceRepo;

    private InsuranceService insuranceService;

    @BeforeEach
        void setUp() {
            InsuranceMapper insuranceMapper = new InsuranceMapper();
            insuranceService = new InsuranceServiceImp(
                                    insuranceRepo,
                                    insuranceMapper);
        }


    @Test
    void shouldReturnGetInsuranceById() {
        Long id = 1L;
        Insurance insurance = createInsurance(id, "OSDE", "30-12345678-9");
        when(insuranceRepo.findById(id)).thenReturn(Optional.of(insurance));

        InsuranceResponse response = insuranceService.getInsuranceById(id);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("OSDE", response.name());
        assertEquals("30-12345678-9", response.cuit());
        verify(insuranceRepo).findById(id);
    }

    @Test
    void shouldThrowResourceNotFoundWhenInsuranceDoesNotExistById() {
        Long id = 99L;
        when(insuranceRepo.findById(id)).thenReturn(Optional.empty());
        var ex = assertThrows(ResourceNotFoundException.class, () -> insuranceService.getInsuranceById(id));
        assertEquals("Insurance not found with id " + id, ex.getMessage());
        verify(insuranceRepo).findById(id);
    }

    @Test
    void shouldReturnGetInsuranceByName() {
        String name = "OSDE";
        Insurance insurance = createInsurance(1L, name, "30-12345678-9");
        when(insuranceRepo.findByName(name)).thenReturn(Optional.of(insurance));

        InsuranceResponse response = insuranceService.getInsuranceByName(name);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(name, response.name());
        assertEquals("30-12345678-9", response.cuit());
        verify(insuranceRepo).findByName(name);
    }

    @Test
    void shouldThrowResourceNotFoundWhenInsuranceDoesNotExistByName() {
        String name = "NOEXIST";
        when(insuranceRepo.findByName(name)).thenReturn(Optional.empty());
        var ex = assertThrows(ResourceNotFoundException.class, () -> insuranceService.getInsuranceByName(name));
        assertEquals("Insurance not found with name " + name, ex.getMessage());
        verify(insuranceRepo).findByName(name);
    }

    @Test
    void shouldReturnAllInsurances() {
        Insurance insurance1 = createInsurance(1L, "OSDE", "30-12345678-9");
        Insurance insurance2 = createInsurance(2L, "SWISS MEDICAL", "30-98765432-1");
        when(insuranceRepo.findAll()).thenReturn(List.of(insurance1, insurance2));

        var result = insuranceService.getAllInsurances();
        assertNotNull(result);
        assertEquals(2L, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("OSDE", result.get(0).name());
        assertEquals("30-12345678-9", result.get(0).cuit());
        assertEquals(2L, result.get(1).id());
        assertEquals("SWISS MEDICAL", result.get(1).name());
        assertEquals("30-98765432-1", result.get(1).cuit());
        verify(insuranceRepo).findAll();
    }

    @Test
    void shouldReturnAllInsurancesEmptyList() {
        when(insuranceRepo.findAll()).thenReturn(List.of());

        var result = insuranceService.getAllInsurances();
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(insuranceRepo).findAll();
    }

    @Test
    void shouldCreateInsuranceSuccessfully() {
        InsuranceCreateRequest request = new InsuranceCreateRequest("OSDE", "30-12345678-9");
        when(insuranceRepo.findByName("OSDE")).thenReturn(Optional.empty());
        when(insuranceRepo.existsByCuit("30123456789")).thenReturn(false);
        when(insuranceRepo.save(any(Insurance.class))).thenAnswer(inv -> {
            Insurance insurance = inv.getArgument(0);
            insurance.setId(1L);
            return insurance;
        });

        InsuranceResponse response = insuranceService.createInsurance(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("OSDE", response.name());
        assertEquals("30-12345678-9", response.cuit());
        verify(insuranceRepo).save(any(Insurance.class));
    }

    @Test
    void shouldThrowStatusConflictWhenCreatingInsuranceWithExistingName() {
        String name = "OSDE";
        Insurance existingInsurance = createInsurance(1L, name, "30-12345678-9");
        when(insuranceRepo.findByName(name)).thenReturn(Optional.of(existingInsurance));

        InsuranceCreateRequest request = new InsuranceCreateRequest(name, "30-98765432-1");

        var ex = assertThrows(StatusConflictException.class, () -> insuranceService.createInsurance(request));
        assertEquals("Insurance with name " + name + " already exists", ex.getMessage());
        verify(insuranceRepo).findByName(name);
    }

    @Test
    void shouldThrowConflictWhenCuitAlreadyExists() {
        InsuranceCreateRequest request = new InsuranceCreateRequest("OSDE", "30-12345678-9");

        when(insuranceRepo.findByName("OSDE")).thenReturn(Optional.empty());
        when(insuranceRepo.existsByCuit("30123456789")).thenReturn(true);

        var ex = assertThrows(StatusConflictException.class,
                () -> insuranceService.createInsurance(request));

        assertEquals("Insurance with CUIT 30-12345678-9 already exists", ex.getMessage());

        verify(insuranceRepo).existsByCuit("30123456789");
        verify(insuranceRepo, never()).save(any());
    }

    @Test
    void shouldUpdateInsuranceNameSuccessfully() {
        Long id = 1L;
        Insurance insurance = createInsurance(id, "OSDE", "30-12345678-9");
        when(insuranceRepo.findById(id)).thenReturn(Optional.of(insurance));
        when(insuranceRepo.existsByNameAndIdNot("SWISS MEDICAL", id)).thenReturn(false);
        when(insuranceRepo.save(any(Insurance.class))).thenAnswer(inv -> inv.getArgument(0));

        InsuranceUpdateRequest updateRequest = new InsuranceUpdateRequest("SWISS MEDICAL", null);

        InsuranceResponse response = insuranceService.updateInsurance(id, updateRequest);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("SWISS MEDICAL", response.name());
        assertEquals("30-12345678-9", response.cuit());
        assertEquals("SWISS MEDICAL", insurance.getName());
        assertEquals("30-12345678-9", insurance.getCuit());
        verify(insuranceRepo).findById(id);
        verify(insuranceRepo).existsByNameAndIdNot("SWISS MEDICAL", id);
        verify(insuranceRepo).save(insurance);
    }

    @Test
    void shouldUpdateInsuranceCuitSuccessfully() {
        Long id = 1L;
        Insurance insurance = createInsurance(id, "OSDE", "30123456789");

        when(insuranceRepo.findById(id)).thenReturn(Optional.of(insurance));
        when(insuranceRepo.existsByCuitAndIdNot("30987654321", id)).thenReturn(false);
        when(insuranceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InsuranceUpdateRequest request = new InsuranceUpdateRequest(null, "30-98765432-1");

        InsuranceResponse response = insuranceService.updateInsurance(id, request);

        assertEquals("30987654321", insurance.getCuit());
        assertEquals(1L, response.id());
        assertEquals("OSDE", response.name());
        assertEquals("30-98765432-1", response.cuit());
        verify(insuranceRepo).findById(id);
        verify(insuranceRepo).existsByCuitAndIdNot("30987654321", id);
        verify(insuranceRepo).save(insurance);
    }

    @Test
    void shouldThrowConflictWhenUpdatingWithExistingCuit() {
        Long id = 1L;
        Insurance insurance = createInsurance(id, "OSDE", "30123456789");

        when(insuranceRepo.findById(id)).thenReturn(Optional.of(insurance));
        when(insuranceRepo.existsByCuitAndIdNot("30987654321", id)).thenReturn(true);

        InsuranceUpdateRequest request = new InsuranceUpdateRequest(null, "30-98765432-1");

        var ex = assertThrows(StatusConflictException.class,
                () -> insuranceService.updateInsurance(id, request));

        assertEquals("Insurance with CUIT 30-98765432-1 already exists", ex.getMessage());

        verify(insuranceRepo, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenUpdatingWithExistingName() {
        Long id = 1L;
        Insurance insurance = createInsurance(id, "OSDE", "30123456789");

        when(insuranceRepo.findById(id)).thenReturn(Optional.of(insurance));
        when(insuranceRepo.existsByNameAndIdNot("SWISS MEDICAL", id)).thenReturn(true);

        InsuranceUpdateRequest request = new InsuranceUpdateRequest("SWISS MEDICAL", null);

        var ex = assertThrows(StatusConflictException.class,
                () -> insuranceService.updateInsurance(id, request));

        assertEquals("Insurance with name SWISS MEDICAL already exists", ex.getMessage());

        verify(insuranceRepo, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundWhenUpdatingNonexistentInsurance() {
        Long id = 99L;
        InsuranceUpdateRequest updateRequest = new InsuranceUpdateRequest("SWISS MEDICAL", "30-98765432-1");
        when(insuranceRepo.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> insuranceService.updateInsurance(id, updateRequest));
        assertEquals("Insurance not found with id " + id, ex.getMessage());
        verify(insuranceRepo).findById(id);
    }

    // Helper
    private Insurance createInsurance(Long id, String name, String cuit) {
        Insurance insurance = new Insurance();
        insurance.setId(id);
        insurance.setName(name);
        insurance.setCuit(cuit);
        return insurance;
    }
}
