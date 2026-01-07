package com.hairbooking.reservation.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.model.User;
import org.hibernate.Hibernate;

public class SalonDTO {
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private List<String> employeeNames;

    private Long ownerId;
    private String ownerFirstName;
    private String ownerLastName;
    private String ownerBirthDate;
    private String ownerEmail;
    private String ownerPhoneNumber;
    private String ownerUsername;
    private Double latitude;
    private Double longitude;

    public SalonDTO(Long id, String name, String address, String phoneNumber, String email, List<String> employeeNames,
            Long ownerId, String ownerFirstName, String ownerLastName, String ownerBirthDate,
            String ownerEmail, String ownerPhoneNumber, String ownerUsername, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.employeeNames = employeeNames;
        this.ownerId = ownerId;
        this.ownerFirstName = ownerFirstName;
        this.ownerLastName = ownerLastName;
        this.ownerBirthDate = ownerBirthDate;
        this.ownerEmail = ownerEmail;
        this.ownerPhoneNumber = ownerPhoneNumber;
        this.ownerUsername = ownerUsername;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public SalonDTO(Salon salon) {
        this.name = salon.getName();
        this.address = salon.getAddress();
        this.phoneNumber = salon.getPhoneNumber();
        this.email = salon.getEmail();
        this.employeeNames = salon.getEmployees() != null
                ? salon.getEmployees().stream().map(User::getUsername).collect(Collectors.toList())
                : List.of();
        this.latitude = salon.getLatitude();
        this.longitude = salon.getLongitude();

        if (Hibernate.isInitialized(salon.getOwner())) { // ✅ Provjera da li je učitan
            User owner = salon.getOwner();
            this.ownerId = owner.getId();
            this.ownerFirstName = owner.getFirstName();
            this.ownerLastName = owner.getLastName();
            this.ownerBirthDate = owner.getBirthDate() != null ? owner.getBirthDate().toString() : "N/A";
            this.ownerEmail = owner.getEmail();
            this.ownerPhoneNumber = owner.getPhoneNumber();
            this.ownerUsername = owner.getUsername();
        } else {
            this.ownerId = null;
            this.ownerFirstName = "N/A";
            this.ownerLastName = "N/A";
            this.ownerBirthDate = "N/A";
            this.ownerEmail = "N/A";
            this.ownerPhoneNumber = "N/A";
            this.ownerUsername = "N/A";
        }
    }

    // GETTERI & SETTERI
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getEmployeeNames() {
        return employeeNames;
    }

    public void setEmployeeNames(List<String> employeeNames) {
        this.employeeNames = employeeNames;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerFirstName() {
        return ownerFirstName;
    }

    public void setOwnerFirstName(String ownerFirstName) {
        this.ownerFirstName = ownerFirstName;
    }

    public String getOwnerLastName() {
        return ownerLastName;
    }

    public void setOwnerLastName(String ownerLastName) {
        this.ownerLastName = ownerLastName;
    }

    public String getOwnerBirthDate() {
        return ownerBirthDate;
    }

    public void setOwnerBirthDate(String ownerBirthDate) {
        this.ownerBirthDate = ownerBirthDate;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerPhoneNumber() {
        return ownerPhoneNumber;
    }

    public void setOwnerPhoneNumber(String ownerPhoneNumber) {
        this.ownerPhoneNumber = ownerPhoneNumber;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
