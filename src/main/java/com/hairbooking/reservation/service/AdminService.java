package com.hairbooking.reservation.service;

import com.hairbooking.reservation.model.Admin;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.repository.AdminRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Admin saveAdmin(Admin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }

    public Admin findByUsernameOrEmail(String identifier) {
        return adminRepository.findByUsernameOrEmail(identifier, identifier)
                .orElse(null);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Admin updateAdmin(Long id, Admin updatedAdmin) {
        Admin existingAdmin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + id));

        // Ažuriraj samo potrebna polja
        if (updatedAdmin.getUsername() != null) existingAdmin.setUsername(updatedAdmin.getUsername());
        if (updatedAdmin.getEmail() != null) existingAdmin.setEmail(updatedAdmin.getEmail());
        if (updatedAdmin.getPassword() != null) existingAdmin.setPassword(updatedAdmin.getPassword()); // Lozinka treba biti enkodirana
        if (updatedAdmin.getRole() != null) existingAdmin.setRole(updatedAdmin.getRole());

        return adminRepository.save(existingAdmin);
    }

    public void deleteAdmin(Long id){
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + id));
        adminRepository.delete(admin);
    }

    // Verifikacija lozinke
    public boolean verifyPassword(Admin admin, String rawPassword) {
        return passwordEncoder.matches(rawPassword, admin.getPassword());
    }
}
