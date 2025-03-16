package com.hairbooking.reservation.dto;

import com.hairbooking.reservation.model.Appointment;
import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentDTO {
    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public AppointmentDTO(Appointment appointment) {
        this.id = appointment.getId();
        this.date = appointment.getDate();
        this.startTime = appointment.getStartTime();
        this.endTime = appointment.getEndTime();
    }

    // Getteri
    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}
