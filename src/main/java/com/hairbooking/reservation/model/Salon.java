package com.hairbooking.reservation.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "salons")
public class Salon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salon_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String email;

    @OneToMany
    private List<User> employees;

    // Constructors
    public Salon() {}

    public Salon(User owner, String name, String address, String phoneNumber, String email, List<User> employees) {
        this.owner = owner;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.employees = employees;
    }

    public Salon(User owner, String name, String address) {
        this.owner = owner;
        this.name = name;
        this.address = address;
    }

    public Salon(User owner, String name, String address, String phoneNumber) {
        this.owner = owner;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public Salon(User owner, String name, String address, String phoneNumber, String email) {
        this.owner = owner;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // GETTER and SETTERS

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
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

    public List<User> getEmployees() {
        return employees;
    }

    public void setEmployees(List<User> employees) {
        this.employees = employees;
    }

}
