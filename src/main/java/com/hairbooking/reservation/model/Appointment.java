package com.hairbooking.reservation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "calendar_id", nullable = false)
    @JsonIgnore
    private Calendar calendar;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceInSalon serviceInSalon; // Povezano sa uslugom

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer; // Povezano sa korisnikom koji je rezervisao termin

    // Constructor

    public Appointment() {

    }

    public Appointment(LocalDate date, LocalTime startTime, LocalTime endTime, Calendar calendar, ServiceInSalon serviceInSalon, User customer, Long id) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.calendar = calendar;
        this.serviceInSalon = serviceInSalon;
        this.customer = customer;
        this.id = id;
    }

    // Getteri i setteri

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public ServiceInSalon getService() {
        return serviceInSalon;
    }

    public void setService(ServiceInSalon serviceInSalon) {
        this.serviceInSalon = serviceInSalon;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }
}
