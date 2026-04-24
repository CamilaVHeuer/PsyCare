package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.exception.*;
import com.camicompany.PsyCare.mapper.PatientMapper;
import com.camicompany.PsyCare.mapper.TutorMapper;
import com.camicompany.PsyCare.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.camicompany.PsyCare.dto.patientDTO.*;
import com.camicompany.PsyCare.model.*;
import com.camicompany.PsyCare.repository.InsuranceRepository;
import com.camicompany.PsyCare.repository.TutorRepository;
import org.junit.jupiter.api.Test;


import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceImpTest {

    @Mock private PatientRepository patientRepo;
    @Mock private TutorRepository tutorRepo;
    @Mock private InsuranceRepository insuranceRepo;
    @Mock private TutorService tutorService;

    private PatientServiceImp patientService;

    @Captor private ArgumentCaptor<Patient> captor;

    @BeforeEach
    void setUp() {
        TutorMapper tutorMapper = new TutorMapper();
        PatientMapper patientMapper = new PatientMapper(tutorMapper);
        patientService = new PatientServiceImp(patientRepo,
                                                patientMapper,
                                                tutorRepo,
                                                insuranceRepo,
                                                tutorService);
    }

    @Test
    void shouldReturnPatientAdultWhenExists() {
        Long id = 1L;
        Patient patient = createPatient(id, "Juan", "Pérez", "12345678",
                LocalDate.of(2000, 1, 1), "12345678");
        patient.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.findById(id)).thenReturn(Optional.of(patient));

        PatientResponse result = patientService.getPatientById(id);

        assertNotNull(result);
        assertEquals(1L, result.patientId());
        assertEquals("Juan", result.firstname());
        assertEquals("Pérez", result.lastname());
        assertEquals("12345678", result.nationalId());
        assertEquals(LocalDate.of(2000, 1, 1), result.birthDate());
        assertEquals("12345678", result.phone());
        assertEquals(PatientStatus.ACTIVE, result.status());
        assertEquals(Period.between(patient.getBirthDate(), LocalDate.now()).getYears(), result.age());

        verify(patientRepo).findById(id);
    }

    @Test
    void shouldReturnPatientMinorWhenExists() {
        // Arrange
        Long id = 1L;
        Tutor tutor = createTutor(1L, "Pedro", "Pérez", "20-12345678-1", TutorRelation.FATHER);

        Patient patient = createPatient(id,"Juan", "Pérez", "12345678", LocalDate.now().minusYears(10), "12345678");
        patient.setStatus(PatientStatus.ACTIVE);
        patient.setTutor(tutor);
        when(patientRepo.findById(id)).thenReturn(Optional.of(patient));

        // Act
        PatientResponse result = patientService.getPatientById(id);

        // Assert
        assertNotNull(result);
        assertEquals("Juan", result.firstname());
        assertEquals("Pérez", result.lastname());
        assertEquals("12345678", result.nationalId());
        assertEquals(LocalDate.now().minusYears(10), result.birthDate());
        assertEquals("12345678", result.phone());
        assertEquals(PatientStatus.ACTIVE, result.status());
        assertEquals(Period.between(patient.getBirthDate(), LocalDate.now()).getYears(), result.age());

        // Tutor asserts
        assertNotNull(result.tutor());
        assertEquals(tutor.getFirstname(), result.tutor().firstname());
        assertEquals(tutor.getLastname(), result.tutor().lastname());
        assertEquals(tutor.getCuil(), result.tutor().cuil());
        assertEquals(tutor.getRelation(), result.tutor().relation());

        verify(patientRepo).findById(id);

    }

   @Test
    void shouldThrowResourceNotFoundWhenPatientDoesNotExist() {
        Long id = 99L;
        when(patientRepo.findById(id)).thenReturn(Optional.empty());

        var ex =  assertThrows(ResourceNotFoundException.class, () -> patientService.getPatientById(id));
        assertEquals("Patient not found with id: " + id, ex.getMessage());
        verify(patientRepo).findById(id);
    }

    @Test
    void shouldReturnAllPatients() {
        Patient patient1 = createPatient(1L, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678");
        patient1.setId(1L);
        patient1.setStatus(PatientStatus.ACTIVE);

        Patient patient2 = createPatient(2L, "Ana", "Gomez", "87654321", LocalDate.of(2010, 1, 1), "87654321");
        patient2.setId(2L);
        patient2.setStatus(PatientStatus.ACTIVE);

        when(patientRepo.findAll()).thenReturn(List.of(patient1, patient2));

        var result = patientService.getAllPatients();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).patientId());
        assertEquals("Juan", result.get(0).firstname());
        assertEquals("Pérez", result.get(0).lastname());
        assertEquals(Period.between(patient1.getBirthDate(), LocalDate.now()).getYears(), result.get(0).age());
        assertEquals("Ana", result.get(1).firstname());
        assertEquals("Gomez", result.get(1).lastname());
        assertEquals(Period.between(patient2.getBirthDate(), LocalDate.now()).getYears(), result.get(1).age());

        verify(patientRepo).findAll();
    }

    @Test
    void shouldReturnAllPatientsEmptyList() {

        when(patientRepo.findAll()).thenReturn(List.of());
        var result = patientService.getAllPatients();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(patientRepo).findAll();
    }

   @Test
    void shouldCreateAdultPatientSuccessfully() {

        PatientCreateRequest request = createPatientRequest(
                "Juan", "Pérez", "12345678",
                LocalDate.of(2000, 1, 1), "12345678",
                null, null, null, null);

        when(patientRepo.existsByNationalId("12345678")).thenReturn(false);

        Patient patientEntity = createPatient(1L, "Juan", "Pérez", "12345678",
                LocalDate.of(2000, 1, 1), "12345678");
        patientEntity.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.save(any(Patient.class))).thenReturn(patientEntity);

        PatientResponse response = patientService.createPatient(request);

        assertNotNull(response);
        assertEquals("Juan", response.firstname());
        assertEquals("Pérez", response.lastname());
        assertEquals("12345678", response.nationalId());
            assertEquals(LocalDate.of(2000, 1, 1), response.birthDate());
        assertEquals("12345678", response.phone());
        assertEquals(Period.between(request.birthDate(), LocalDate.now()).getYears(), response.age());
        assertNull(response.insuranceName());
        assertNull(response.insuranceNumber());
        assertEquals(PatientStatus.ACTIVE, response.status());

        verify(patientRepo).existsByNationalId("12345678");
        verify(patientRepo).save(captor.capture());

        Patient savedPatient = captor.getValue();
        assertEquals("Juan", savedPatient.getFirstname());
        assertEquals("Pérez", savedPatient.getLastname());
        assertEquals("12345678", savedPatient.getNationalId());
        assertEquals(LocalDate.of(2000, 1, 1), savedPatient.getBirthDate());
        assertEquals("12345678", savedPatient.getPhone());
        assertEquals(PatientStatus.ACTIVE, savedPatient.getStatus());

       assertNull(savedPatient.getTutor());
       assertNull(savedPatient.getInsurance());
    }

    @Test
    void shouldCreateAdultPatientWithInsuranceSuccessfully() {

        Insurance insurance = createInsurance(1L, "OSDE", "30-12345678-9");
        PatientCreateRequest request = createPatientRequest(
                "Juan", "Pérez", "12345678",
                LocalDate.of(2000, 1, 1), "12345678",
                null, null, 1L, "1234");

        when(patientRepo.existsByNationalId("12345678")).thenReturn(false);
        when(insuranceRepo.findById(1L)).thenReturn(Optional.of(insurance));

        Patient patientEntity = createPatient(1L, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678");
        patientEntity.setInsurance(insurance);
        patientEntity.setInsuranceNumber("1234");
        patientEntity.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.save(any(Patient.class))).thenReturn(patientEntity);

        PatientResponse response = patientService.createPatient(request);

        assertNotNull(response);
        assertEquals("Juan", response.firstname());
        assertEquals("Pérez", response.lastname());
        assertEquals("12345678", response.nationalId());
        assertEquals(LocalDate.of(2000, 1, 1), response.birthDate());
        assertEquals("12345678", response.phone());
        assertEquals(Period.between(request.birthDate(), LocalDate.now()).getYears(), response.age());
        assertEquals("OSDE", response.insuranceName());
        assertEquals("1234", response.insuranceNumber());
        assertEquals(PatientStatus.ACTIVE, response.status());

        verify(patientRepo).existsByNationalId("12345678");
        verify(insuranceRepo).findById(1L);
        verify(patientRepo).save(captor.capture());
        Patient savedPatient = captor.getValue();

        assertEquals("Juan", savedPatient.getFirstname());
        assertEquals("Pérez", savedPatient.getLastname());
        assertEquals("12345678", savedPatient.getNationalId());
        assertEquals(LocalDate.of(2000, 1, 1), savedPatient.getBirthDate());
        assertEquals("12345678", savedPatient.getPhone());
        assertEquals("OSDE", savedPatient.getInsurance().getName());
        assertEquals("1234", savedPatient.getInsuranceNumber());
        assertEquals(PatientStatus.ACTIVE, savedPatient.getStatus());

        assertNull(savedPatient.getTutor());
    }

   @Test
    void shouldThrowDuplicateNationalIdExceptionOnCreate() {
        PatientCreateRequest request = createPatientRequest(
                "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678", null, null, null, null);
        when(patientRepo.existsByNationalId("12345678")).thenReturn(true);

        var ex = assertThrows(DuplicateNationalIdException.class, () -> patientService.createPatient(request));
        assertEquals("Patient with this national ID already exists", ex.getMessage());

        verify(patientRepo).existsByNationalId("12345678");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenInsuranceDoesNotExistOnCreate() {
        PatientCreateRequest request = createPatientRequest(
                "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678", null, null, 99L, "1234");
        when(patientRepo.existsByNationalId("12345678")).thenReturn(false);
        when(insuranceRepo.findById(99L)).thenReturn(Optional.empty());


        var ex = assertThrows(ResourceNotFoundException.class, () -> patientService.createPatient(request));
        assertEquals("Insurance not found with id: 99", ex.getMessage());
    }
    @Test
    void shouldCreateMinorPatientWithInsuranceAndTutorSuccessfully() {
        Insurance insurance = createInsurance(1L, "OSDE", "30-12345678-9");

        TutorCreateRequest tutorRequest = createTutorRequest("Pedro", "Pérez", "12345678","20-12345678-1", TutorRelation.FATHER);
        PatientCreateRequest request = createPatientRequest(
                "Juan", "Pérez", "12345678",
                LocalDate.now().minusYears(10), "12345678",
                null, tutorRequest, 1L, "1234");

        Tutor tutorSaved = createTutor(1L, "Pedro", "Pérez", "20-12345678-1", TutorRelation.FATHER);
        when(patientRepo.existsByNationalId("12345678")).thenReturn(false);
        when(insuranceRepo.findById(1L)).thenReturn(Optional.of(insurance));
        when(tutorService.findOrCreateTutor(tutorRequest)).thenReturn(tutorSaved);

        Patient patientEntity = createPatient(1L, "Juan", "Pérez", "12345678", LocalDate.now().minusYears(10), "12345678");
        patientEntity.setTutor(tutorSaved);
        patientEntity.setInsurance(insurance);
        patientEntity.setInsuranceNumber("1234");
        patientEntity.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.save(any(Patient.class))).thenReturn(patientEntity);

        PatientResponse response = patientService.createPatient(request);

        assertNotNull(response);
        assertEquals("Juan", response.firstname());
        assertEquals("Pérez", response.lastname());
        assertEquals("12345678", response.nationalId());
        assertEquals(LocalDate.now().minusYears(10), response.birthDate());
        assertEquals("12345678", response.phone());
        assertEquals(Period.between(request.birthDate(), LocalDate.now()).getYears(), response.age());
        assertEquals("OSDE", response.insuranceName());
        assertEquals("1234", response.insuranceNumber());
        assertEquals(PatientStatus.ACTIVE, response.status());

        // Tutor asserts
        assertNotNull(response.tutor());
        assertEquals(tutorSaved.getFirstname(), response.tutor().firstname());
        assertEquals(tutorSaved.getLastname(), response.tutor().lastname());
        assertEquals(tutorSaved.getCuil(), response.tutor().cuil());
        assertEquals(tutorSaved.getRelation(), response.tutor().relation());

        verify(patientRepo).existsByNationalId("12345678");
        verify(tutorService).findOrCreateTutor(tutorRequest);
        verify(insuranceRepo).findById(1L);

        verify(patientRepo).save(captor.capture());
        Patient savedPatient = captor.getValue();
        assertEquals("Juan", savedPatient.getFirstname());
        assertEquals("Pérez", savedPatient.getLastname());
        assertEquals("12345678", savedPatient.getNationalId());
        assertEquals(LocalDate.now().minusYears(10), savedPatient.getBirthDate());
        assertEquals("12345678", savedPatient.getPhone());
        assertEquals("OSDE", savedPatient.getInsurance().getName());
        assertEquals("1234", savedPatient.getInsuranceNumber());
        assertEquals(PatientStatus.ACTIVE, savedPatient.getStatus());

    }

    @Test
    void shouldCreateMinorPatientWithoutInsuranceAndTutorSuccessfully() {

        TutorCreateRequest tutorRequest = createTutorRequest("Pedro", "Pérez", "12345678","20-12345678-1", TutorRelation.FATHER);
        PatientCreateRequest request = createPatientRequest(
                "Juan", "Pérez", "12345678",
                LocalDate.of(2000, 1, 1), "12345678",
                null, tutorRequest, null, null);

        Tutor tutorSaved = createTutor(1L, "Pedro", "Pérez", "20-12345678-1", TutorRelation.FATHER);

        when(patientRepo.existsByNationalId("12345678")).thenReturn(false);
        when(tutorService.findOrCreateTutor(tutorRequest)).thenReturn(tutorSaved);

        Patient patientEntity = createPatient(1L, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678");
        patientEntity.setTutor(tutorSaved);
        patientEntity.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.save(any(Patient.class))).thenReturn(patientEntity);


        PatientResponse response = patientService.createPatient(request);

        assertNotNull(response);
        assertEquals("Juan", response.firstname());
        assertEquals("Pérez", response.lastname());
        assertEquals("12345678", response.nationalId());
        assertEquals(LocalDate.of(2000, 1, 1), response.birthDate());
        assertEquals("12345678", response.phone());
        assertEquals(Period.between(request.birthDate(), LocalDate.now()).getYears(), response.age());
        assertEquals(PatientStatus.ACTIVE, response.status());


        assertNotNull(response.tutor());
        assertEquals(tutorSaved.getFirstname(), response.tutor().firstname());
        assertEquals(tutorSaved.getLastname(), response.tutor().lastname());
        assertEquals(tutorSaved.getCuil(), response.tutor().cuil());
        assertEquals(tutorSaved.getRelation(), response.tutor().relation());

        verify(patientRepo).existsByNationalId("12345678");
        verify(patientRepo).save(any(Patient.class));
        verify(tutorService).findOrCreateTutor(tutorRequest);
    }

    @Test
    void shouldCreateMinorPatientWithInsuranceAndExistingTutorSuccessfully() {
        Insurance insurance = createInsurance(1L, "OSDE", "30-12345678-9");

        PatientCreateRequest request = createPatientRequest(
                "Juan", "Pérez", "12345678",
                LocalDate.now().minusYears(15), null,
                1L, null, 1L, "1234");

        Tutor tutor = createTutor(1L, "Pedro", "Pérez", "20-12345678-1", TutorRelation.FATHER);


        when(patientRepo.existsByNationalId("12345678")).thenReturn(false);
        when(insuranceRepo.findById(1L)).thenReturn(Optional.of(insurance));
        when(tutorRepo.findById(1L)).thenReturn(Optional.of(tutor));

        Patient patientEntity = createPatient(1L, "Juan", "Pérez", "12345678", LocalDate.now().minusYears(15), null);
        patientEntity.setTutor(tutor);
        patientEntity.setInsurance(insurance);
        patientEntity.setInsuranceNumber("1234");
        patientEntity.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.save(any(Patient.class))).thenReturn(patientEntity);

        PatientResponse response = patientService.createPatient(request);

        assertNotNull(response);
        assertEquals("Juan", response.firstname());
        assertEquals("Pérez", response.lastname());
        assertEquals("12345678", response.nationalId());
        assertEquals(LocalDate.now().minusYears(15), response.birthDate());
        assertNull(response.phone());
        assertEquals(Period.between(request.birthDate(), LocalDate.now()).getYears(), response.age());
        assertEquals("OSDE", response.insuranceName());
        assertEquals("1234", response.insuranceNumber());
        assertEquals(PatientStatus.ACTIVE, response.status());

        assertNotNull(response.tutor());
        assertEquals(tutor.getFirstname(), response.tutor().firstname());
        assertEquals(tutor.getLastname(), response.tutor().lastname());
        assertEquals(tutor.getCuil(), response.tutor().cuil());
        assertEquals(tutor.getRelation(), response.tutor().relation());

        verify(patientRepo).existsByNationalId("12345678");
        verify(insuranceRepo).findById(1L);
        verify(tutorRepo).findById(1L);
        verify(patientRepo).save(any(Patient.class));
        verify(patientRepo).save(captor.capture());

        Patient savedPatient = captor.getValue();
        assertEquals("Juan", savedPatient.getFirstname());
        assertEquals("Pérez", savedPatient.getLastname());
        assertEquals("12345678", savedPatient.getNationalId());
        assertEquals(LocalDate.now().minusYears(15), savedPatient.getBirthDate());
        assertNull(savedPatient.getPhone());
        assertEquals("OSDE", savedPatient.getInsurance().getName());
        assertEquals("1234", savedPatient.getInsuranceNumber());
        assertEquals(PatientStatus.ACTIVE, savedPatient.getStatus());
    }
    @Test
    void shouldCreateMinorPatientWithoutInsuranceAndExistingTutor() {

        PatientCreateRequest request = createPatientRequest(
                "Juan", "Pérez", "12345678",
                LocalDate.now().minusYears(15), null,
                1L, null, null, null);

        Tutor tutor = createTutor(1L, "Pedro", "Pérez", "20-12345678-1", TutorRelation.FATHER);


        when(patientRepo.existsByNationalId("12345678")).thenReturn(false);
        when(tutorRepo.findById(1L)).thenReturn(Optional.of(tutor));

        Patient patientEntity = createPatient(1L, "Juan", "Pérez", "12345678", LocalDate.now().minusYears(15), null);
        patientEntity.setTutor(tutor);
        patientEntity.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.save(any(Patient.class))).thenReturn(patientEntity);

        PatientResponse response = patientService.createPatient(request);

        assertNotNull(response);
        assertEquals("Juan", response.firstname());
        assertEquals("Pérez", response.lastname());
        assertEquals("12345678", response.nationalId());
        assertEquals(LocalDate.now().minusYears(15), response.birthDate());
        assertNull(response.phone());
        assertEquals(Period.between(request.birthDate(), LocalDate.now()).getYears(), response.age());
        assertEquals(PatientStatus.ACTIVE, response.status());

        assertNotNull(response.tutor());
        assertEquals(tutor.getFirstname(), response.tutor().firstname());
        assertEquals(tutor.getLastname(), response.tutor().lastname());
        assertEquals(tutor.getCuil(), response.tutor().cuil());
        assertEquals(tutor.getRelation(), response.tutor().relation());

        verify(patientRepo).existsByNationalId("12345678");
        verify(tutorRepo).findById(1L);
        verify(patientRepo).save(any(Patient.class));
        verify(patientRepo).save(captor.capture());

        Patient savedPatient = captor.getValue();
        assertEquals("Juan", savedPatient.getFirstname());
        assertEquals("Pérez", savedPatient.getLastname());
        assertEquals("12345678", savedPatient.getNationalId());
        assertEquals(LocalDate.now().minusYears(15), savedPatient.getBirthDate());
        assertNull(savedPatient.getPhone());
        assertEquals(PatientStatus.ACTIVE, savedPatient.getStatus());
    }

    @Test
    void shouldThrowMissingTutorExceptionForMinorWithoutTutor() {
        PatientCreateRequest request = new PatientCreateRequest(
                "Ana", "Gomez", "87654321",
                LocalDate.now().minusYears(15),
                null, null, null, null, null);
        when(patientRepo.existsByNationalId("87654321")).thenReturn(false);

        var ex = assertThrows(MissingTutorException.class, () -> patientService.createPatient(request));
        assertEquals("Minor patients require tutor information", ex.getMessage());
    }

   @Test
    void shouldThrowConflictingTutorInformationExceptionOnCreate() {

       TutorCreateRequest tutorRequest = createTutorRequest("Pedro", "Gomez", "12345678","20-12345678-1", TutorRelation.FATHER);
       PatientCreateRequest request = new PatientCreateRequest(
                "Ana", "Gomez", "87654321",
                LocalDate.now().minusYears(15), null,
               1L, tutorRequest, null, null);
       when(patientRepo.existsByNationalId("87654321")).thenReturn(false);

       var ex =  assertThrows(ConflictingTutorInformationException.class, () -> patientService.createPatient(request));
       assertEquals("Provide either tutorId or tutor info, not both", ex.getMessage());
    }

   @Test
    void shouldThrowResourceNotFoundWhenTutorIdDoesNotExistOnCreate() {
        PatientCreateRequest request = new PatientCreateRequest(
                "Ana", "Gomez", "87654321", LocalDate.now().minusYears(15),
                null, 1L, null, null, null
        );

        when(patientRepo.existsByNationalId("87654321")).thenReturn(false);
        when(tutorRepo.findById(1L)).thenReturn(Optional.empty());

        var ex =  assertThrows(ResourceNotFoundException.class, () -> patientService.createPatient(request));
        assertEquals("Tutor not found with id: 1", ex.getMessage());
    }

    @Test
    void shouldUpdateAdultPatientBasicFieldsSuccessfully() {
        // Arrange
        Long id = 1L;
        Patient existingPatient = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678");
        existingPatient.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.findById(id)).thenReturn(Optional.of(existingPatient));

        PatientUpdateRequest updateRequest = createPatientUpdateRequest(
                "Juana", "Pérez", null, null, "87654321", null, null, null, null);

        Patient updatedPatient = createPatient(id, "Juana", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "87654321");
        updatedPatient.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.save(any(Patient.class))).thenReturn(updatedPatient);

        // Act
        PatientResponse response = patientService.updatePatient(id, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Juana", response.firstname());
        assertEquals("Pérez", response.lastname());
        assertEquals("12345678", response.nationalId());
        assertEquals(LocalDate.of(2000, 1, 1), response.birthDate());
        assertEquals("87654321", response.phone());
        assertEquals(PatientStatus.ACTIVE, response.status());
        verify(patientRepo).save(any(Patient.class));
    }

    @Test
    void shouldUpdateMinorPatientTutorSuccessfully() {
        // Arrange
        Long id = 1L;
        Patient existingPatient = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.now().minusYears(10), "12345678");
        existingPatient.setStatus(PatientStatus.ACTIVE);
        Tutor oldTutor = createTutor(2L, "Pedro", "Pérez", "20-11111111-1", TutorRelation.LEGAL_GUARDIAN);
        existingPatient.setTutor(oldTutor);
        when(patientRepo.findById(id)).thenReturn(Optional.of(existingPatient));

        Tutor newTutor = createTutor(3L, "Ana", "Gómez", "27-22222222-2", TutorRelation.MOTHER);
        when(tutorRepo.findById(3L)).thenReturn(Optional.of(newTutor));

        PatientUpdateRequest updateRequest = createPatientUpdateRequest(
                null, null, null, null, null, 3L, null, null, null);

        Patient updatedPatient = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.now().minusYears(10), "12345678");
        updatedPatient.setStatus(PatientStatus.ACTIVE);
        updatedPatient.setTutor(newTutor);
        when(patientRepo.save(any(Patient.class))).thenReturn(updatedPatient);

        // Act
        PatientResponse response = patientService.updatePatient(id, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Ana", response.tutor().firstname());
        assertEquals("Gómez", response.tutor().lastname());
        assertEquals("27-22222222-2", response.tutor().cuil());
        assertEquals(TutorRelation.MOTHER, response.tutor().relation());
        verify(patientRepo).save(any(Patient.class));
        verify(tutorRepo).findById(3L);
    }

    //---------------------------------------------------------------------------------
    // UPDATE
    //_________________________________________________________________________________

    @Test
    void shouldUpdatePatientInsuranceSuccessfully() {
        Long id = 1L;
        Patient existingPatient = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678");
        existingPatient.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.findById(id)).thenReturn(Optional.of(existingPatient));

        Insurance newInsurance = createInsurance(2L, "SWISS MEDICAL", "30-22222222-2");
        when(insuranceRepo.findById(2L)).thenReturn(Optional.of(newInsurance));

        PatientUpdateRequest updateRequest = createPatientUpdateRequest(
                null, null, null, null, null, null, null, 2L, "9999");

        Patient updatedPatient = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678");
        updatedPatient.setStatus(PatientStatus.ACTIVE);
        updatedPatient.setInsurance(newInsurance);
        updatedPatient.setInsuranceNumber("9999");
        when(patientRepo.save(any(Patient.class))).thenReturn(updatedPatient);

        PatientResponse response = patientService.updatePatient(id, updateRequest);

        assertNotNull(response);
        assertEquals("SWISS MEDICAL", response.insuranceName());
        assertEquals("9999", response.insuranceNumber());
        verify(patientRepo).save(any(Patient.class));
        verify(insuranceRepo).findById(2L);
    }

    @Test
    void shouldUpdateBasicFieldsSuccessfully() {
        Long id = 1L;

        Patient existing = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.of(2000,1,1), "12345678");
        existing.setStatus(PatientStatus.ACTIVE);

        when(patientRepo.findById(id)).thenReturn(Optional.of(existing));

        PatientUpdateRequest request = createPatientUpdateRequest(
                "Juana", null, null, null, "87654321",
                null, null, null, null);

        when(patientRepo.save(any())).thenReturn(existing);

        PatientResponse res = patientService.updatePatient(id, request);

        assertEquals("Juana", existing.getFirstname());
        assertEquals("87654321", existing.getPhone());
        assertEquals(1L, res.patientId());

        verify(patientRepo).save(any(Patient.class));
    }

    @Test
    void shouldNotFailWhenUpdatingWithSameNationalId() {
        Long id = 1L;
        Patient existing = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.of(2000,1,1), "12345678");

        when(patientRepo.findById(id)).thenReturn(Optional.of(existing));

        PatientUpdateRequest request = createPatientUpdateRequest(
                null, null, "12345678", null, "87654321",
                null, null, null, null);

        when(patientRepo.save(any())).thenReturn(existing);
        assertDoesNotThrow(() -> patientService.updatePatient(id, request));
    }

    @Test
    void shouldNotModifyAnythingWhenAllFieldsNull() {
        Long id = 1L;

        Patient existing = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.of(2000,1,1), "12345678");

        when(patientRepo.findById(id)).thenReturn(Optional.of(existing));
        when(patientRepo.save(any())).thenReturn(existing);

        PatientUpdateRequest request = createPatientUpdateRequest(
                null, null, null, null, null,
                null, null, null, null);

        patientService.updatePatient(id, request);

        assertEquals("Juan", existing.getFirstname());
        assertEquals("12345678", existing.getPhone());
    }

    @Test
    void shouldThrowConflictingTutorInformationExceptionOnUpdate() {
        TutorCreateRequest tutorRequest = createTutorRequest("Pedro", "Gomez", "12345678","20-12345678-1", TutorRelation.FATHER);
        Long id = 1L;
        Patient existingPatient = new Patient();
        existingPatient.setId(id);
        when(patientRepo.findById(id)).thenReturn(Optional.of(existingPatient));

        PatientUpdateRequest updateRequest = createPatientUpdateRequest(
                null, null, null, LocalDate.now().minusYears(15),  null, 1L, tutorRequest, null, null);

        var ex = assertThrows(ConflictingTutorInformationException.class, () -> patientService.updatePatient(id, updateRequest));
        assertEquals("Provide either tutorId or tutor info, not both", ex.getMessage());
    }

    @Test
    void shouldThrowMissingTutorExceptionForMinorWithoutTutorOnUpdate() {
        Long id = 1L;
        Patient existingPatient = new Patient();
        existingPatient.setId(id);
        when(patientRepo.findById(id)).thenReturn(Optional.of(existingPatient));

        PatientUpdateRequest updateRequest = createPatientUpdateRequest(
                null, null, null, LocalDate.now().minusYears(15) , null, null, null, null, null
        );

        var ex = assertThrows(MissingTutorException.class, () -> patientService.updatePatient(id, updateRequest));
        assertEquals("Minor patients require tutor information", ex.getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundWhenTutorIdDoesNotExistOnUpdate() {
        Long id = 1L;
        Patient existingPatient = new Patient();
        existingPatient.setId(id);
        when(patientRepo.findById(id)).thenReturn(Optional.of(existingPatient));
        when(tutorRepo.findById(id)).thenReturn(Optional.empty());

        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                null, null, null, null, null, id, null, null, null
        );

        var ex = assertThrows(ResourceNotFoundException.class, () -> patientService.updatePatient(id, updateRequest));
        assertEquals("Tutor not found with id: 1", ex.getMessage());
    }

   @Test
    void shouldThrowResourceNotFoundWhenInsuranceIdDoesNotExistOnUpdate() {
        Long id = 1L;
        Patient existingPatient = new Patient();
        existingPatient.setId(id);
        when(patientRepo.findById(id)).thenReturn(Optional.of(existingPatient));
        when(insuranceRepo.findById(id)).thenReturn(Optional.empty());

        PatientUpdateRequest updateRequest = new PatientUpdateRequest(
                null, null, null, null, null, null, null, id, "1234"
        );

         var ex = assertThrows(ResourceNotFoundException.class, () -> patientService.updatePatient(id, updateRequest));
        assertEquals("Insurance not found with id: 1", ex.getMessage());
        verify(insuranceRepo).findById(id);
    }

    @Test
    void shouldThrowDuplicateNationalIdExceptionOnUpdate() {
        Long id = 1L;
        Patient existingPatient = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678");
        existingPatient.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.findById(id)).thenReturn(Optional.of(existingPatient));

        when(patientRepo.existsByNationalId("87654321")).thenReturn(true);

        PatientUpdateRequest updateRequest = createPatientUpdateRequest(
                null, null, "87654321", null, null, null, null, null, null);

        var ex = assertThrows(DuplicateNationalIdException.class, () -> patientService.updatePatient(id, updateRequest));
        assertEquals("Patient with this national ID already exists", ex.getMessage());
        verify(patientRepo).existsByNationalId("87654321");
    }

    //---------------------------------------------------------------------------------
    // STATUS CHANGES
    //_________________________________________________________________________________
    @Test
    void shouldDischargePatientSuccessfully() {
        Long id = 1L;
        Patient patient = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678");
        patient.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.findById(id)).thenReturn(Optional.of(patient));

        Patient dischargedPatient = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678");
        dischargedPatient.setStatus(PatientStatus.DISCHARGED);
        when(patientRepo.save(any(Patient.class))).thenReturn(dischargedPatient);

        PatientResponse response = patientService.dischargePatient(id);

        assertNotNull(response);
        assertEquals(PatientStatus.DISCHARGED, response.status());
        verify(patientRepo).save(any(Patient.class));
    }
    @Test
    void shouldReactivatePatientSuccessfully() {
        Long id = 1L;
        Patient patient = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678");
        patient.setStatus(PatientStatus.DISCHARGED);
        when(patientRepo.findById(id)).thenReturn(Optional.of(patient));

        Patient reactivatedPatient = createPatient(id, "Juan", "Pérez", "12345678", LocalDate.of(2000, 1, 1), "12345678");
        reactivatedPatient.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.save(any(Patient.class))).thenReturn(reactivatedPatient);

        PatientResponse response = patientService.reactivePatient(id);

        assertNotNull(response);
        assertEquals(PatientStatus.ACTIVE, response.status());
        verify(patientRepo).save(any(Patient.class));
    }

  @Test
    void shouldThrowStatusConflictExceptionWhenDischargingAlreadyDischargedPatient() {
        Long id = 1L;
        Patient patient = new Patient();
        patient.setId(id);
        patient.setStatus(PatientStatus.DISCHARGED);
        when(patientRepo.findById(id)).thenReturn(Optional.of(patient));

        var ex = assertThrows(StatusConflictException.class, () -> patientService.dischargePatient(id));
        assertEquals("Patient is already discharged", ex.getMessage());
    }

    @Test
    void shouldThrowStatusConflictExceptionWhenReactivatingAlreadyActivePatient() {

        Long id = 1L;
        Patient patient = new Patient();
        patient.setId(id);
        patient.setStatus(PatientStatus.ACTIVE);
        when(patientRepo.findById(id)).thenReturn(Optional.of(patient));

        var ex =  assertThrows(StatusConflictException.class, () -> patientService.reactivePatient(id));
        assertEquals("Patient is already active", ex.getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundWhenDischargingNonexistentPatient() {
        Long id = 99L;
        when(patientRepo.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> patientService.dischargePatient(id));
        assertEquals("Patient not found with id: 99", ex.getMessage());
        verify(patientRepo).findById(id);
    }

    @Test
    void shouldThrowResourceNotFoundWhenReactivatingNonexistentPatient() {
        Long id = 99L;
        when(patientRepo.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> patientService.reactivePatient(id));
        assertEquals("Patient not found with id: 99", ex.getMessage());
        verify(patientRepo).findById(id);
    }


    //helper methods
    private Patient createPatient(Long id, String firstname, String lastname,
                                  String nationalId, LocalDate birthDate, String phone) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setFirstname(firstname);
        patient.setLastname(lastname);
        patient.setNationalId(nationalId);
        patient.setBirthDate(birthDate);
        patient.setPhone(phone);
        return patient;
    }

    private PatientCreateRequest createPatientRequest(String firstname, String lastname,
                                                      String nationalId, LocalDate birthDate, String phone, Long tutorId, TutorCreateRequest tutor, Long insuranceId, String insuranceNumber) {
        return new PatientCreateRequest(firstname, lastname, nationalId, birthDate, phone, tutorId, tutor, insuranceId , insuranceNumber);
    }

    private Tutor createTutor(Long id, String firstname, String lastname, String cuil, TutorRelation relation) {
        Tutor tutor = new Tutor();
        tutor.setId(id);
        tutor.setFirstname(firstname);
        tutor.setLastname(lastname);
        tutor.setCuil(cuil);
        tutor.setRelation(relation);
        return tutor;
    }

    private TutorCreateRequest createTutorRequest(String firstname, String lastname, String phone,  String cuil, TutorRelation relation) {
        return new TutorCreateRequest(firstname, lastname, phone,  cuil, relation);
    }

    private Insurance createInsurance(Long id, String name, String cuit) {
        Insurance insurance = new Insurance();
        insurance.setId(id);
        insurance.setName(name);
        insurance.setCuit(cuit);
        return insurance;
    }

    private PatientUpdateRequest createPatientUpdateRequest(String firstname, String lastname, String nationalId, LocalDate birthDate,
                                                      String phone,  Long tutorId, TutorCreateRequest tutor, Long insuranceId, String insuranceNumber) {
        return new PatientUpdateRequest(firstname, lastname, nationalId, birthDate, phone, tutorId, tutor, insuranceId, insuranceNumber);
    }
}
