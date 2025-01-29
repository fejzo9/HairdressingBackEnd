package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.model.Admin;
import com.hairbooking.reservation.model.LoginRequest;
import com.hairbooking.reservation.model.Role;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.security.JwtUtil;
import com.hairbooking.reservation.service.AdminService;
import com.hairbooking.reservation.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173") // Dozvoljava zahteve sa frontenda
public class LoginController {

    private final UserService userService;
    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    public LoginController(UserService userService, AdminService adminService, JwtUtil jwtUtil) {

        this.userService = userService;
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String identifier = loginRequest.getIdentifier();
        String password = loginRequest.getPassword();

        User user = userService.findByUsernameOrEmail(identifier);
        Admin admin = adminService.findByUsernameOrEmail(identifier);

        if (user == null || !userService.verifyPassword(user, password)) {
            if (admin == null || !adminService.verifyPassword(admin, password)) {
                return ResponseEntity.status(401).body("Invalid username/email or password");
            }
        }

        String token;
        Role role;
        String username;
        Long id;

        if(user != null){
            token = jwtUtil.generateToken(user.getUsername(), user.getRole().toString());
            role = user.getRole();
            username = user.getUsername();
            id = user.getId();
        } else {
            token = jwtUtil.generateToken(admin.getUsername(), admin.getRole().toString());
            role = admin.getRole();
            username = admin.getUsername();
            id = admin.getId();
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Login successful!");
        response.put("token", token);
        response.put("role", role.toString());
        response.put("username", username);
        response.put("id", String.valueOf(id));
        return ResponseEntity.ok(response);
    }
}

