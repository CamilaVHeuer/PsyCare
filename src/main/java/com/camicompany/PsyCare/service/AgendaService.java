package com.camicompany.PsyCare.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AgendaService {
    public List<LocalDateTime> getAvailableSlots(LocalDate date);

    public boolean isSlotAvailable(LocalDateTime dateTime);
}
