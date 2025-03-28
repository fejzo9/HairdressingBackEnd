package com.hairbooking.reservation.dto;

import com.hairbooking.reservation.model.WorkingHours;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class WorkingHoursDTO {
    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isDayOff;

    public WorkingHoursDTO(WorkingHours wh) {
        this.id = wh.getId();
        this.dayOfWeek = wh.getDayOfWeek();
        this.startTime = wh.getStartTime();
        this.endTime = wh.getEndTime();
        this.isDayOff = wh.isDayOff();
    }

    // Getteri i setteri

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isDayOff() {
        return isDayOff;
    }

    public void setDayOff(boolean dayOff) {
        isDayOff = dayOff;
    }
}

