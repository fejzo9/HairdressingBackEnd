package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.model.ChangePasswordRequest;
import com.hairbooking.reservation.model.Role;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.service.UserService;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 🔐 Omogućava samo ADMINIMA da vide sve korisnike
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // 🔐 Omogućava ADMINIMA i vlasnicima naloga da vide korisnika po ID-u
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // 🔐 Omogućava samo ADMINIMA da mijenjaju korisnike
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
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
}
