package com.hairbooking.reservation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "salons")
public class Salon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "salons_employees",
            joinColumns = @JoinColumn(name = "salon_salon_id"),
            inverseJoinColumns = @JoinColumn(name = "employees_user_id")
    )
    private List<User> employees;

    // Lista slika kao binarni podaci
    @ElementCollection
    @CollectionTable(name = "salon_images", joinColumns = @JoinColumn(name = "salon_id"))
    @Column(name = "image_data")
    private List<byte[]> images = new ArrayList<>();

    // Lista formata slika
    @ElementCollection
    @CollectionTable(name = "salon_images_metadata", joinColumns = @JoinColumn(name = "salon_id"))
    @Column(name = "image_type")
    private List<String> imageTypes = new ArrayList<>();

    // Usluge salona
    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore // Sprječava vraćanje cijelog salona u Service JSON-u
    private List<Service> services = new ArrayList<>();

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

    public Salon(String name, String address, String phoneNumber, String email){
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // ✅ KONSTRUKTOR BEZ SLIKA
    public Salon(String name, String address, String phoneNumber, String email, User owner, List<User> employees) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.owner = owner;
        this.employees = employees != null ? employees : new ArrayList<>();
        this.images = new ArrayList<>();
        this.imageTypes = new ArrayList<>();
    }

    // ✅ KONSTRUKTOR SA SLIKAMA
    public Salon(String name, String address, String phoneNumber, String email, User owner, List<User> employees, List<byte[]> images, List<String> imageTypes) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.owner = owner;
        this.employees = employees != null ? employees : new ArrayList<>();
        this.images = images != null ? images : new ArrayList<>();
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

    public List<byte[]> getImages() { return images;}

    public void setImages(List<byte[]> images) { this.images = images; }

    public List<String> getImageTypes() { return imageTypes; }

    public void setImageTypes(List<String> imageTypes) { this.imageTypes = imageTypes; }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }
}
