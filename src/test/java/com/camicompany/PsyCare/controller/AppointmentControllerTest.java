
package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.security.config.filter.JwtTokenValidator;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentCreateRequest;
import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentResponse;
import com.camicompany.PsyCare.dto.appointmentDTO.AppointmentUpdateRequest;
import com.camicompany.PsyCare.exception.ResourceNotFoundException;
import com.camicompany.PsyCare.exception.StatusConflictException;
import com.camicompany.PsyCare.model.AppointmentStatus;
import com.camicompany.PsyCare.model.AppointmentType;
import com.camicompany.PsyCare.model.AppointmentPaymentStatus;
import com.camicompany.PsyCare.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class, excludeAutoConfiguration =  SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class AppointmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AppointmentService appointmentServ;
    @MockitoBean private JwtTokenValidator jwtTokenValidator;
    @Autowired private ObjectMapper objectMapper;


    private static final String BASE_URL = "/api/v1/appointments";

    @Test
    void getAppointmentByIdShouldReturn200() throws Exception {
        AppointmentResponse response = new AppointmentResponse(
                1L, LocalDateTime.of(2024, 12, 31, 14, 0), BigDecimal.valueOf(30000),
                2L, "Juan Pérez", "12345678", AppointmentType.GENERAL, AppointmentStatus.SCHEDULED, AppointmentPaymentStatus.PENDING
        );
        when(appointmentServ.getAppointmentById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.appDateTime").value("2024-12-31T14:00:00"))
                .andExpect(jsonPath("$.price").value(30000))
                .andExpect(jsonPath("$.patientId").value(2L))
                .andExpect(jsonPath("$.patientFullName").value("Juan Pérez"))
                .andExpect(jsonPath("$.patientPhone").value("12345678"))
                .andExpect(jsonPath("$.type").value(AppointmentType.GENERAL.name()))
                .andExpect(jsonPath("$.status").value(AppointmentStatus.SCHEDULED.name()))
                .andExpect(jsonPath("$.appointmentPaymentStatus").value(AppointmentPaymentStatus.PENDING.name()));

        verify(appointmentServ).getAppointmentById(1L);
    }


    @Test
    void getAppointmentByIdShouldReturn404() throws Exception {
        when(appointmentServ.getAppointmentById(99L)).thenThrow(new ResourceNotFoundException("Appointment not found with id: 99"));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createAppointmentShouldReturn201() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.of(2026, 12, 31, 14, 0), BigDecimal.valueOf(30000),
                "Juan", "Pérez", "12345678", null, AppointmentType.GENERAL
        );
        AppointmentResponse response = new AppointmentResponse(
                1L, request.appDateTime(), request.price(), null, "Juan Pérez", "12345678",
                AppointmentType.GENERAL, AppointmentStatus.SCHEDULED, AppointmentPaymentStatus.PENDING
        );
        when(appointmentServ.createAppointment(any(AppointmentCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BASE_URL + "/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.appDateTime").value("2026-12-31T14:00:00"))
                .andExpect(jsonPath("$.price").value(30000))
                .andExpect(jsonPath("$.patientFullName").value("Juan Pérez"))
                .andExpect(jsonPath("$.patientPhone").value("12345678"))
                .andExpect(jsonPath("$.type").value(AppointmentType.GENERAL.name()))
                .andExpect(jsonPath("$.status").value(AppointmentStatus.SCHEDULED.name()))
                .andExpect(jsonPath("$.appointmentPaymentStatus").value(AppointmentPaymentStatus.PENDING.name()));

        verify(appointmentServ).createAppointment(request);
    }

    @Test
    void createAppointmentShouldReturn400WhenDateIsPast() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.now().minusDays(1), BigDecimal.valueOf(30000),
                "Juan", "Pérez", "12345678", null, AppointmentType.GENERAL
        );
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The appointment date cannot be in the past")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createAppointmentShouldReturn400WhenPriceIsNegative() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.now().plusDays(1), BigDecimal.valueOf(-100),
                "Juan", "Pérez", "12345678", null, AppointmentType.GENERAL
        );
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The price must be a positive number")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createAppointmentShouldReturn400WhenPhoneIsTooLong() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.now().plusDays(1), BigDecimal.valueOf(1000),
                "Juan", "Pérez", "123456789012345678901", null, AppointmentType.GENERAL
        );
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The phone number must not exceed 15 characters")))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void createAppointmentShouldReturn400WhenTypeIsNull() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.now().plusDays(1),
                BigDecimal.valueOf(30000),
                "Juan", "Perez", "12345678",
                null,
                null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("The appointment type is required")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createAppointmentShouldReturn409WhenSlotIsOccupied() throws Exception {
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                LocalDateTime.now().plusDays(1), BigDecimal.valueOf(1000),
                "Juan", "Pérez", "12345678", null, AppointmentType.GENERAL
        );
        when(appointmentServ.createAppointment(any(AppointmentCreateRequest.class)))
                .thenThrow(new StatusConflictException("Slot already booked"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Slot already booked"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateAppointmentShouldReturn200() throws Exception {
        LocalDateTime appDateTime = LocalDateTime.now().plusDays(2);
        AppointmentUpdateRequest updateRequest = new AppointmentUpdateRequest(
                appDateTime, BigDecimal.valueOf(30000),
                "Ana", "Gomez", "87654321", null, AppointmentType.GENERAL
        );
        AppointmentResponse response = new AppointmentResponse(
                1L, updateRequest.appDateTime(), updateRequest.price(), null, "Ana Gomez", "87654321",
                AppointmentType.GENERAL, AppointmentStatus.SCHEDULED, AppointmentPaymentStatus.PENDING
        );
        when(appointmentServ.updateAppointment(eq(1L), any(AppointmentUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.appDateTime").exists())
                .andExpect(jsonPath("$.price").value(30000))
                .andExpect(jsonPath("$.patientFullName").value("Ana Gomez"))
                .andExpect(jsonPath("$.patientPhone").value("87654321"))
                .andExpect(jsonPath("$.type").value(AppointmentType.GENERAL.name()))
                .andExpect(jsonPath("$.status").value(AppointmentStatus.SCHEDULED.name()))
                .andExpect(jsonPath("$.appointmentPaymentStatus").value(AppointmentPaymentStatus.PENDING.name()));

        verify(appointmentServ).updateAppointment(eq(1L), any(AppointmentUpdateRequest.class));
    }
    @Test
    void updateAppointmentShouldReturn200WhenUpdateOnlyPrice() throws Exception {
        Long appointmentId = 1L;
        AppointmentUpdateRequest updateRequest = new AppointmentUpdateRequest(
                null,
                BigDecimal.valueOf(50000),
                null,
                null,
                null,
                null,
                null
        );

        AppointmentResponse response = new AppointmentResponse(
                appointmentId,
                LocalDateTime.of(2026, 12, 31, 14, 0), // se mantiene igual
                BigDecimal.valueOf(50000), // actualizado
                2L,
                "Juan Pérez",
                "12345678",
                AppointmentType.GENERAL,
                AppointmentStatus.SCHEDULED,
                AppointmentPaymentStatus.PENDING
        );

        when(appointmentServ.updateAppointment(eq(appointmentId), any(AppointmentUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId))
                .andExpect(jsonPath("$.price").value(50000))
                .andExpect(jsonPath("$.appDateTime").value("2026-12-31T14:00:00"))
                .andExpect(jsonPath("$.patientFullName").value("Juan Pérez"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.appointmentPaymentStatus").value(AppointmentPaymentStatus.PENDING.name()));

        verify(appointmentServ).updateAppointment(eq(appointmentId), any(AppointmentUpdateRequest.class));
    }

    @Test
    void updateAppointmentShouldReturn404() throws Exception {
        AppointmentUpdateRequest updateRequest = new AppointmentUpdateRequest(
                LocalDateTime.now().plusDays(2), BigDecimal.valueOf(2000),
                "Ana", "Gomez", "87654321", null, AppointmentType.GENERAL
        );
        when(appointmentServ.updateAppointment(eq(99L), any(AppointmentUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Appointment not found with id: 99"));

        mockMvc.perform(patch(BASE_URL + "/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateAppointmentShouldReturn409WhenSlotIsOccupied() throws Exception {
        AppointmentUpdateRequest updateRequest = new AppointmentUpdateRequest(
                LocalDateTime.now().plusDays(2), BigDecimal.valueOf(30000),
                "Ana", "Gomez", "87654321", null, AppointmentType.GENERAL
        );
        when(appointmentServ.updateAppointment(eq(1L), any(AppointmentUpdateRequest.class)))
                .thenThrow(new StatusConflictException("Slot already booked"));

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Slot already booked"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void cancelAppointmentShouldReturn200() throws Exception {
        AppointmentResponse response = new AppointmentResponse(
                1L, LocalDateTime.now().plusDays(1), BigDecimal.valueOf(1000),
                2L, "Juan Pérez", "12345678", AppointmentType.GENERAL, AppointmentStatus.CANCELLED, AppointmentPaymentStatus.PENDING
        );
        when(appointmentServ.cancelAppointment(1L)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(appointmentServ).cancelAppointment(1L);
    }

    @Test
    void cancelAppointmentShouldReturn404() throws Exception {
        when(appointmentServ.cancelAppointment(99L)).thenThrow(new ResourceNotFoundException("Appointment not found with id: 99"));

        mockMvc.perform(put(BASE_URL + "/99/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void markAsAttendedShouldReturn200() throws Exception {
        AppointmentResponse response = new AppointmentResponse(
                1L, LocalDateTime.now().plusDays(1), BigDecimal.valueOf(1000),
                2L, "Juan Pérez", "12345678", AppointmentType.GENERAL, AppointmentStatus.ATTENDED, AppointmentPaymentStatus.PAID
        );
        when(appointmentServ.markAsAttended(1L)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/mark-as-attended"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ATTENDED"));

        verify(appointmentServ).markAsAttended(1L);
    }

    @Test
    void markAsAttendedShouldReturn404() throws Exception {
        when(appointmentServ.markAsAttended(99L)).thenThrow(new ResourceNotFoundException("Appointment not found with id: 99"));

        mockMvc.perform(put(BASE_URL + "/99/mark-as-attended"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void markAsNoShowShouldReturn200() throws Exception {
        AppointmentResponse response = new AppointmentResponse(
                1L, LocalDateTime.now().plusDays(1), BigDecimal.valueOf(1000),
                2L, "Juan Pérez", "12345678", AppointmentType.GENERAL, AppointmentStatus.NO_SHOW, AppointmentPaymentStatus.PENDING
        );
        when(appointmentServ.markAsNoShow(1L)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/mark-as-no-show"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("NO_SHOW"));

        verify(appointmentServ).markAsNoShow(1L);
    }

    @Test
    void markAsNoShowShouldReturn404() throws Exception {
        when(appointmentServ.markAsNoShow(99L)).thenThrow(new ResourceNotFoundException("Appointment not found with id: 99"));

        mockMvc.perform(put(BASE_URL + "/99/mark-as-no-show"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found with id: 99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    
}
