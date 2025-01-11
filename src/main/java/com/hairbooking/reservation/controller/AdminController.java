package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.model.Admin;
import com.hairbooking.reservation.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<Admin> addAdmin(@RequestBody Admin admin) {
        Admin savedAdmin = adminService.saveAdmin(admin);
        return ResponseEntity.ok(savedAdmin);
    }

    @GetMapping("/{username}")
    public ResponseEntity<Admin> getAdminByUsername(@PathVariable String username) {
        Admin admin = adminService.findByUsername(username);
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(admin);
    }

    // GET endpoint za dohvaćanje svih admina
    @GetMapping
    public ResponseEntity<List<Admin>> getAllAdmins() {
        List<Admin> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }
}
