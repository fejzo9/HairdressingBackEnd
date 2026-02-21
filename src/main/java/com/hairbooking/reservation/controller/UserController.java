package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.dto.UserDTO;
import com.hairbooking.reservation.model.ChangePasswordRequest;
import com.hairbooking.reservation.model.Role;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.service.UserService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Optional<User> userOptional = userService.getUserByUsername(username);
        if (userOptional.isPresent()) {
            UserDTO userDTO = new UserDTO(userOptional.get());
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

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userService.updateUser(id, updatedUser);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

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
        boolean isChanged = userService.changePassword(request.getUsername(), request.getOldPassword(),
                request.getNewPassword());

        if (isChanged) {
            return ResponseEntity.ok("Lozinka uspješno promijenjena!");
        } else {
            return ResponseEntity.badRequest().body("Neispravna stara lozinka ili korisnik ne postoji!");
        }
    }

    // ✅ Upload profilne slike — čuva fajl na disk, vraća path
    @PostMapping("/{id}/upload-profile-picture")
    @PreAuthorize("hasAnyRole('USER', 'OWNER', 'HAIRDRESSER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> uploadProfilePicture(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        System.out.println("Primljen fajl: " + file.getOriginalFilename());

        String path = userService.uploadProfilePicture(id, file);

        if (path != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(path);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška pri uploadu slike!");
        }
    }

    // ✅ Dohvatanje path-a profilne slike korisnika
    @GetMapping("/{id}/profile-picture-path")
    public ResponseEntity<String> getProfilePicturePath(@PathVariable Long id) {
        String path = userService.getProfilePicturePath(id);

        if (path != null) {
            return ResponseEntity.ok(path);
        }

        return ResponseEntity.notFound().build();
    }
}
