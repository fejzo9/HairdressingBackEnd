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


    public SalonDTO(Long id, String name, String address, String phoneNumber, String email, List<String> employeeNames) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.employeeNames = employeeNames;
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
}
