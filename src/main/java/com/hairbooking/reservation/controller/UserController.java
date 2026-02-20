package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.dto.AppointmentHistoryDTO;
import com.hairbooking.reservation.dto.UserDTO;
import com.hairbooking.reservation.model.ChangePasswordRequest;
import com.hairbooking.reservation.model.Role;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.service.AppointmentService;
import com.hairbooking.reservation.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AppointmentService appointmentService;

    public UserController(UserService userService, AppointmentService appointmentService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserDTOById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username){
        Optional<User> userOptional = userService.getUserByUsername(username);
        if (userOptional.isPresent()) {
            UserDTO userDTO = new UserDTO(userOptional.get()); // Mapiranje u DTO
            return ResponseEntity.ok(userDTO);
        } else {
        return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // 🔐 Omogućava samo ADMINIMA da mijenjaju korisnike
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userService.updateUser(id, updatedUser);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    // 🔐 Omogućava samo ADMINIMA da brišu korisnike
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Dohvati korisnike po ulozi
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'OWNER')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    // Promjena lozinke korisnika
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        boolean isChanged = userService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());

        if (isChanged) {
            return ResponseEntity.ok("Lozinka uspješno promijenjena!");
        } else {
            return ResponseEntity.badRequest().body("Neispravna stara lozinka ili korisnik ne postoji!");
        }
    }

    // ✅ Verify email address using token
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        boolean verified = userService.verifyEmail(token);
        if (verified) {
            return ResponseEntity.ok("Email verified successfully!");
        }
        return ResponseEntity.badRequest().body("Invalid or expired verification token.");
    }

    // ✅ Resend verification email
    @PostMapping("/resend-verification-email")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam("email") String email) {
        boolean sent = userService.resendVerificationEmail(email);
        if (sent) {
            return ResponseEntity.ok("Verification email sent. Check your inbox.");
        }
        return ResponseEntity.badRequest().body("Email not found or already verified.");
    }

    // ✅ Upload slike
    @PostMapping("/{id}/upload-profile-picture")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('HAIRDRESSER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> uploadProfilePicture(@PathVariable Long id, @RequestParam("file") MultipartFile file) {

        System.out.println("Primljen fajl: " + file.getOriginalFilename());

        boolean success = userService.uploadProfilePicture(id, file);

        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Profilna slika uspjesno dodana!");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška pri uploadu slike!");
        }
    }

    // ✅ Dohvatanje slike korisnika
    @GetMapping("/{id}/profile-picture")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable Long id) {
        System.out.println("🔍 Pokušaj dohvatanja profilne slike za korisnika sa ID: " + id);

        Optional<User> userOptional = userService.getUserByIdOptional(id);

        if (userOptional.isPresent() && userOptional.get().getProfilePicture() != null) {
            System.out.println("✅ Profilna slika pronađena za korisnika: " + id);
            byte[] imageBytes = userOptional.get().getProfilePicture();
            String contentType = userOptional.get().getProfilePictureType();

            // Postavljanje odgovarajućeg content-type headera
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType)); // Može biti IMAGE_JPEG ako koristiš jpg slike
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        }

        System.out.println("❌ Profilna slika NIJE pronađena za korisnika: " + id);
        return ResponseEntity.notFound().build();
    }

    // ✅ GET - Dohvatanje historije termina korisnika
    @GetMapping("/{userId}/appointments")
    public ResponseEntity<Page<AppointmentHistoryDTO>> getUserAppointments(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        Page<AppointmentHistoryDTO> appointments = appointmentService.getUserAppointments(userId, status, page, size, sort);
        return ResponseEntity.ok(appointments);
    }
}

