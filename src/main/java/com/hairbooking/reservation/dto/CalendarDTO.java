package com.hairbooking.reservation.dto;

import com.hairbooking.reservation.model.Appointment;
import com.hairbooking.reservation.model.Calendar;

import java.util.List;
import java.util.stream.Collectors;

public class CalendarDTO {
    private Long id;
    private List<AppointmentDTO> appointments;

    // Konstruktor koji konvertuje Calendar u CalendarDTO
    public CalendarDTO(Calendar calendar) {
        this.id = calendar.getId();
        this.appointments = calendar.getAppointments().stream()
                .map(AppointmentDTO::new)  // Konvertujemo Appointment u AppointmentDTO
                .collect(Collectors.toList());
    }

    // Getteri
    public Long getId() {
        return id;
    }

    public List<AppointmentDTO> getAppointments() {
        return appointments;
    }
}

