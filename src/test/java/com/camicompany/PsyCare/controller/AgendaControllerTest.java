package com.camicompany.PsyCare.controller;

import com.camicompany.PsyCare.security.config.filter.JwtTokenValidator;
import com.camicompany.PsyCare.service.AgendaService;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AgendaController.class, excludeAutoConfiguration =  SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class AgendaControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AgendaService agendaService;

    @MockitoBean private JwtTokenValidator jwtTokenValidator;

    @Test
    void getAvailableSlotsShouldReturn200() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 15);
        when(agendaService.getAvailableSlots(date)).thenReturn(List.of(
                LocalDateTime.of(2026, 11, 2, 13, 0),
                LocalDateTime.of(2026, 11, 2, 13, 40)
        ));

        mockMvc.perform(get("/api/v1/agenda/available-slots")
                        .param("date", date.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0]").value("2026-11-02T13:00:00"))
                .andExpect(jsonPath("$[1]").value("2026-11-02T13:40:00"));
    }
}
