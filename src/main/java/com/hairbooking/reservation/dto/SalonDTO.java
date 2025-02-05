package com.hairbooking.reservation.dto;

import java.util.List;
import com.hairbooking.reservation.model.User;

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

    public SalonDTO(Long id, String name, String address, String phoneNumber, String email, List<String> employeeNames,
                    Long ownerId, String ownerFirstName, String ownerLastName, String ownerBirthDate,
                    String ownerEmail, String ownerPhoneNumber, String ownerUsername) {
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
    }
    // GETTERI & SETTERI
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getEmployeeNames() { return employeeNames; }
    public void setEmployeeNames(List<String> employeeNames) { this.employeeNames = employeeNames; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerFirstName() { return ownerFirstName; }
    public void setOwnerFirstName(String ownerFirstName) { this.ownerFirstName = ownerFirstName; }

    public String getOwnerLastName() { return ownerLastName; }
    public void setOwnerLastName(String ownerLastName) { this.ownerLastName = ownerLastName; }

    public String getOwnerBirthDate() { return ownerBirthDate; }
    public void setOwnerBirthDate(String ownerBirthDate) { this.ownerBirthDate = ownerBirthDate; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getOwnerPhoneNumber() { return ownerPhoneNumber; }
    public void setOwnerPhoneNumber(String ownerPhoneNumber) { this.ownerPhoneNumber = ownerPhoneNumber; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}
