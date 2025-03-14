package com.hairbooking.reservation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "calendars")
public class Calendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "hairdresser_id", nullable = false)
    private User hairdresser; // Svaki kalendar pripada jednom frizeru

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Appointment> appointments = new ArrayList<>();

    //Constructors

    public Calendar(){

    }

    public Calendar(User hairdresser) {
        this.hairdresser = hairdresser;
    }

    public Calendar(Long id, User hairdresser) {
        this.id = id;
        this.hairdresser = hairdresser;
    }

    public Calendar(Long id, User hairdresser, List<Appointment> appointments) {
        this.id = id;
        this.hairdresser = hairdresser;
        this.appointments = appointments;
    }

    // Getteri i setteri

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getHairdresser() {
        return hairdresser;
    }

    public void setHairdresser(User hairdresser) {
        this.hairdresser = hairdresser;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
}

