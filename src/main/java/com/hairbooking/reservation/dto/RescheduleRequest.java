package com.hairbooking.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class RescheduleRequest {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime newStartTime;

    public RescheduleRequest() {}

    public RescheduleRequest(LocalDate newDate, LocalTime newStartTime) {
        this.newDate = newDate;
        this.newStartTime = newStartTime;
    }

    public LocalDate getNewDate() {
        return newDate;
    }

    public void setNewDate(LocalDate newDate) {
        this.newDate = newDate;
    }

    public LocalTime getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(LocalTime newStartTime) {
        this.newStartTime = newStartTime;
    }
}
