package com.camicompany.PsyCare.service;

import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorResponse;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRelationRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.exception.StatusConflictException;
import com.camicompany.PsyCare.mapper.TutorMapper;
import com.camicompany.PsyCare.model.Tutor;
import com.camicompany.PsyCare.model.TutorRelation;
import com.camicompany.PsyCare.repository.TutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TutorServiceImpTest {

    @Mock private TutorRepository tutorRepo;

    private TutorServiceImp tutorService;

    @Captor private ArgumentCaptor<Tutor> captor;

    @BeforeEach
    public void setUp() {
        TutorMapper tutorMapper = new TutorMapper();
        tutorService = new TutorServiceImp(tutorRepo, tutorMapper);
    }

    @Test
    void shouldCreateTutorSuccessfully() {
        TutorCreateRequest request = createTutorCreateRequest("Ana", "Gomez", "87654321", "27-87654321-2", TutorRelation.MOTHER);
        when(tutorRepo.existsByCuil("27876543212")).thenReturn(false);

        Tutor savedTutor = createTutor(2L, "Ana", "Gomez", "87654321", "27876543212", TutorRelation.MOTHER);
        when(tutorRepo.save(any(Tutor.class))).thenReturn(savedTutor);

        TutorResponse response = tutorService.createTutor(request);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("Ana", response.firstname());
        assertEquals("Gomez", response.lastname());
        assertEquals("87654321", response.phone());
        assertEquals("27-87654321-2", response.cuil());
        assertEquals(TutorRelation.MOTHER, response.relation());

        verify(tutorRepo).save(captor.capture());
        Tutor captured = captor.getValue();
        assertEquals("Ana", captured.getFirstname());
        assertEquals("Gomez", captured.getLastname());
        assertEquals("87654321", captured.getPhone());
        assertEquals("27876543212", captured.getCuil());
        assertEquals(TutorRelation.MOTHER, captured.getRelation());
    }
    @Test
    void shouldThrowStatusConflictWhenCuilAlreadyExistsOnCreate() {
        TutorCreateRequest request = createTutorCreateRequest(
                "Ana", "Gomez", "87654321", "27-87654321-2", TutorRelation.MOTHER);
        when(tutorRepo.existsByCuil("27876543212")).thenReturn(true);

        var ex = assertThrows(StatusConflictException.class, () -> tutorService.createTutor(request));

        assertEquals("The CUIL 27-87654321-2 is already registered for another tutor", ex.getMessage());
        verify(tutorRepo, never()).save(any());
    }

   @Test
    void shouldUpdateTutorSuccessfully() {

        Tutor existingTutor = createTutor(3L, "Pedro", "Pérez", "12345678", "20123456781", TutorRelation.FATHER);
        when(tutorRepo.findById(3L)).thenReturn(Optional.of(existingTutor));
        when(tutorRepo.existsByCuilAndIdNot("20123456781", 3L)).thenReturn(false);

        TutorUpdateRequest updateRequest = createTutorUpdateRequest("Pedro", "Pérez", "99999999", "20-12345678-1");
        when(tutorRepo.save(any(Tutor.class))).thenReturn(existingTutor);

        TutorResponse response = tutorService.updateTutor(3L, updateRequest);

        assertNotNull(response);
        assertEquals(3L, response.id());
        assertEquals("Pedro", response.firstname());
        assertEquals("Pérez", response.lastname());
        assertEquals("99999999", response.phone());
        assertEquals("20-12345678-1", response.cuil());
        assertEquals(TutorRelation.FATHER, response.relation());
        verify(tutorRepo).findById(3L);
        verify(tutorRepo).existsByCuilAndIdNot("20123456781", 3L);
        verify(tutorRepo).save(existingTutor);
    }

    @Test
    void shouldThrowStatusConflictWhenCuilAlreadyExistsOnUpdate() {
        Tutor existingTutor = createTutor(3L, "Pedro", "Pérez", "12345678", "20123456781", TutorRelation.FATHER);
        when(tutorRepo.findById(3L)).thenReturn(Optional.of(existingTutor));

        TutorUpdateRequest request = createTutorUpdateRequest(null, null, null, "27-87654321-2");
        when(tutorRepo.existsByCuilAndIdNot("27876543212", 3L)).thenReturn(true);

        var ex = assertThrows(StatusConflictException.class, () -> tutorService.updateTutor(3L, request));

        assertEquals("The CUIL 27-87654321-2 is already registered for another tutor", ex.getMessage());
        verify(tutorRepo, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundWhenUpdatingNonexistentTutor() {
        when(tutorRepo.findById(99L)).thenReturn(Optional.empty());
        TutorUpdateRequest updateRequest = createTutorUpdateRequest("Pedro", "Pérez", "99999999", "20-12345678-1");

        var ex = assertThrows(ResourceNotFoundException.class, () -> tutorService.updateTutor(99L, updateRequest));
        assertEquals("Tutor not found with id: 99", ex.getMessage());
        verify(tutorRepo).findById(99L);
    }

    @Test
    void shouldChangeRelationTutorPatientSuccessfully() {
        Tutor existingTutor = createTutor(4L, "Carlos", "Lopez", "11112222", "20-11112222-3", TutorRelation.LEGAL_GUARDIAN);
        when(tutorRepo.findById(4L)).thenReturn(Optional.of(existingTutor));

        TutorUpdateRelationRequest relationRequest = createTutorUpdateRelationRequest(TutorRelation.FATHER);
        when(tutorRepo.save(any(Tutor.class))).thenReturn(existingTutor);

        TutorResponse response = tutorService.changeRelationTutorPatient(4L, relationRequest);

        assertNotNull(response);
        assertEquals(4L, response.id());
        assertEquals("Carlos", response.firstname());
        assertEquals("Lopez", response.lastname());
        assertEquals("11112222", response.phone());
        assertEquals("20-11112222-3", response.cuil());
        assertEquals(TutorRelation.FATHER, response.relation());
        verify(tutorRepo).findById(4L);
        verify(tutorRepo).save(existingTutor);
    }

    @Test
    void shouldThrowResourceNotFoundWhenChangingRelationOfNonexistentTutor() {
        // Arrange
        when(tutorRepo.findById(99L)).thenReturn(Optional.empty());
        TutorUpdateRelationRequest relationRequest = createTutorUpdateRelationRequest(TutorRelation.FATHER);

        // Act & Assert
        var ex = assertThrows(ResourceNotFoundException.class, () -> tutorService.changeRelationTutorPatient(99L, relationRequest));
        assertEquals("Tutor not found with id: 99", ex.getMessage());
        verify(tutorRepo).findById(99L);
    }

    @Test
    void shouldReturnExistingTutorInFindOrCreate() {
        TutorCreateRequest request = createTutorCreateRequest(
                "Ana", "Gomez", "12345678", "27-87654321-2", TutorRelation.MOTHER);

        Tutor existing = createTutor(1L, "Ana", "Gomez", "12345678", "27876543212", TutorRelation.MOTHER);
        when(tutorRepo.findByCuil("27876543212")).thenReturn(Optional.of(existing));

        Tutor result = tutorService.findOrCreateTutor(request);

        assertEquals(existing, result);
        verify(tutorRepo, never()).save(any());
    }

    @Test
    void shouldCreateTutorInFindOrCreateWhenNotExists() {
        TutorCreateRequest request = createTutorCreateRequest(
                "Ana", "Gomez", "12345678", "27-87654321-2", TutorRelation.MOTHER);
        when(tutorRepo.findByCuil("27876543212")).thenReturn(Optional.empty());
        when(tutorRepo.save(any(Tutor.class))).thenAnswer(inv -> {;
            Tutor t = inv.getArgument(0);
            t.setId(1L); // Simulate DB assigning an ID
            return t;
        });
       // Tutor saved = createTutor(1L, "Ana", "Gomez", "12345678", "27876543212", TutorRelation.MOTHER);

       // when(tutorRepo.save(any())).thenReturn(saved);
        Tutor result = tutorService.findOrCreateTutor(request);

       // assertEquals(saved, result);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ana", result.getFirstname());
        assertEquals("Gomez", result.getLastname());
        assertEquals("12345678", result.getPhone());
        assertEquals("27876543212", result.getCuil());
        assertEquals(TutorRelation.MOTHER, result.getRelation());
        verify(tutorRepo).save(any(Tutor.class));
    }

    // Helper
    private Tutor createTutor(Long id, String firstname, String lastname, String phone, String cuil, TutorRelation relation) {
        Tutor tutor = new Tutor();
        tutor.setId(id);
        tutor.setFirstname(firstname);
        tutor.setLastname(lastname);
        tutor.setPhone(phone);
        tutor.setCuil(cuil);
        tutor.setRelation(relation);
        return tutor;
    }

    private TutorCreateRequest createTutorCreateRequest(String firstname, String lastname, String phone, String cuil, TutorRelation relation) {
        return new TutorCreateRequest(firstname, lastname, phone, cuil, relation);
    }

    private TutorUpdateRequest createTutorUpdateRequest(String firstname, String lastname, String phone, String cuil) {
        return new TutorUpdateRequest(firstname, lastname, phone, cuil);
    }

    private TutorUpdateRelationRequest createTutorUpdateRelationRequest(TutorRelation relation) {
        return new TutorUpdateRelationRequest(relation);
    }

}
