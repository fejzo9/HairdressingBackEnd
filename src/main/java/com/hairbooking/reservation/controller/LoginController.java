package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.model.LoginRequest;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String identifier = loginRequest.getIdentifier();
        String password = loginRequest.getPassword();

        User user = userService.findByUsernameOrEmail(identifier);

        if (user == null || !userService.verifyPassword(user, password)) {
            return ResponseEntity.status(401).body("Invalid username/email or password");
        }

        return ResponseEntity.ok("Login successful!");
    }

}

