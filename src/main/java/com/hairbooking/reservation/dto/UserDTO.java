package com.hairbooking.reservation.dto;

import com.hairbooking.reservation.model.Role;
import com.hairbooking.reservation.model.User;

public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private Role role;
    private Long calendarId; // Dodajemo samo ako je korisnik frizer

    // Konstruktor koji mapira User -> UserDTO
    public UserDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.role = user.getRole();

        // 🔎 Provjera kalendara prije dodjele vrijednosti
        if ("HAIRDRESSER".equals(user.getRole().toString()) && user.getCalendar() != null) {
            this.calendarId = user.getCalendar().getId();
            System.out.println("✅ Postavljen calendarId: " + this.calendarId);
        } else {
            this.calendarId = null;
            System.out.println("❌ Frizer nema kalendar ili nije HAIRDRESSER");
        }
    }

    // Getteri
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public Long getCalendarId() { return calendarId; }

}

