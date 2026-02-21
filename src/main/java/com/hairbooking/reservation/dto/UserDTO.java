package com.hairbooking.reservation.dto;

import com.hairbooking.reservation.model.Role;
import com.hairbooking.reservation.model.User;

public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String phoneNumber;
    private Role role;
    private Long calendarId;
    private String profilePicturePath;

    // Konstruktor koji mapira User -> UserDTO
    public UserDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.profilePicturePath = user.getProfilePicturePath();

        if ("HAIRDRESSER".equals(user.getRole().toString()) && user.getCalendar() != null) {
            this.calendarId = user.getCalendar().getId();
        } else {
            this.calendarId = null;
        }
    }

    // Getteri
    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }
}
