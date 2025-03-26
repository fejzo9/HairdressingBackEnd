package com.hairbooking.reservation.model;

import jakarta.persistence.Table;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "working_hours")
public class WorkingHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "hairdresser_id", nullable = false)
    private User hairdresser;

    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isDayOff;

    public WorkingHours() {

    }

    public WorkingHours(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public WorkingHours(DayOfWeek dayOfWeek, boolean isDayOff) {
        this.dayOfWeek = dayOfWeek;
        this.isDayOff = isDayOff;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public User getHairdresser() {
        return hairdresser;
    }
    public void setHairdresser(User hairdresser) {
        this.hairdresser = hairdresser;
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
