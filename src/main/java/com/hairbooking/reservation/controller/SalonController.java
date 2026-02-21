package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.dto.SalonDTO;
import com.hairbooking.reservation.dto.SalonRequestDTO;
import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.service.SalonService;
import com.hairbooking.reservation.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/salons")
public class SalonController {

    private final SalonService salonService;
    private final UserService userService;

    public SalonController(SalonService salonService, UserService userService) {
        this.salonService = salonService;
        this.userService = userService;
    }

    @GetMapping
    public List<SalonDTO> getAllSalons() {
        return salonService.getAllSalons();
    }

    @Transactional
    @GetMapping("/{id}")
    public ResponseEntity<SalonDTO> getSalonById(@PathVariable Long id) {
        System.out.println("Zahtjev za salon sa ID-jem: " + id);
        Optional<Salon> salonOptional = salonService.getSalonById(id);

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();
            SalonDTO salonDTO = new SalonDTO(salon);
            System.out.println("Salon pronađen: " + salon.getName());
            return ResponseEntity.ok(salonDTO);
        } else {
            System.out.println("Salon nije pronađen!");
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> createSalon(@RequestBody SalonRequestDTO request) {
        try {
            Salon newSalon = new Salon();
            newSalon.setName(request.getName());
            newSalon.setAddress(request.getAddress());
            newSalon.setPhoneNumber(request.getPhoneNumber());
            newSalon.setEmail(request.getEmail());
            newSalon.setLatitude(request.getLatitude());
            newSalon.setLongitude(request.getLongitude());

            Salon savedSalon = salonService.createSalon(newSalon, request.getOwnerUsername());
            return ResponseEntity.ok(savedSalon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Greška pri dodavanju salona: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Salon> updateSalon(@PathVariable Long id, @RequestBody Salon salon) {
        Salon updatedSalon = salonService.updateSalon(id, salon);
        return updatedSalon != null ? ResponseEntity.ok(updatedSalon) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/employees/add")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SalonDTO> addEmployeeToSalon(@PathVariable Long id, @RequestBody Long employeeId) {
        System.out.println("Dodavanje frizera sa ID: " + employeeId + " u salon ID: " + id);

        Optional<Salon> salonOptional = salonService.getSalonById(id);
        Optional<User> employeeOptional = Optional.ofNullable(userService.getUserById(employeeId));

        if (salonOptional.isPresent() && employeeOptional.isPresent()) {
            Salon salon = salonOptional.get();
            User employee = employeeOptional.get();

            if (!salon.getEmployees().contains(employee)) {
                salon.getEmployees().add(employee);
                salonService.saveSalon(salon);
            }

            return ResponseEntity.ok(new SalonDTO(salon));
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/employees")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SalonDTO> addEmployeesToSalon(@PathVariable Long id, @RequestBody List<Long> employeeIds) {
        System.out.println("Dodavanje liste frizera u salon ID: " + id);

        Optional<Salon> salonOptional = salonService.getSalonById(id);
        if (salonOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Salon salon = salonOptional.get();
        List<User> existingEmployees = salon.getEmployees();
        List<User> newEmployees = userService.findUsersByIds(employeeIds);

        for (User newEmployee : newEmployees) {
            if (!existingEmployees.contains(newEmployee)) {
                existingEmployees.add(newEmployee);
            }
        }
        salon.setEmployees(existingEmployees);
        salonService.saveSalon(salon);

        return ResponseEntity.ok(new SalonDTO(salon));
    }

    @PutMapping("/{id}/employees")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SalonDTO> updateSalonEmployees(@PathVariable Long id, @RequestBody List<Long> employeeIds) {
        System.out.println("Dodavanje zaposlenika u salon ID: " + id);
        Optional<Salon> salonOptional = salonService.getSalonById(id);

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();
            List<User> employees = userService.findUsersByIds(employeeIds);
            salon.setEmployees(employees);
            salonService.saveSalon(salon);
            return ResponseEntity.ok(new SalonDTO(salon));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSalon(@PathVariable Long id) {
        salonService.deleteSalon(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{salonId}/employees/{employeeId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> removeEmployeeFromSalon(@PathVariable Long salonId, @PathVariable Long employeeId) {
        System.out.println("Brisanje zaposlenika ID: " + employeeId + " iz salona ID: " + salonId);

        Optional<Salon> salonOptional = salonService.getSalonById(salonId);
        Optional<User> employeeOptional = userService.findById(employeeId);

        if (salonOptional.isPresent() && employeeOptional.isPresent()) {
            Salon salon = salonOptional.get();
            User employee = employeeOptional.get();

            if (salon.getEmployees().contains(employee)) {
                salon.getEmployees().remove(employee);
                salonService.saveSalon(salon);
                return ResponseEntity.ok("Frizer uspješno uklonjen iz salona.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Frizer nije pronađen u ovom salonu.");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salon ili frizer ne postoje.");
    }

    @DeleteMapping("/{id}/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> removeAllEmployeesFromSalon(@PathVariable Long id) {
        System.out.println("Brisanje svih zaposlenika iz salona ID: " + id);

        Optional<Salon> salonOptional = salonService.getSalonById(id);
        if (salonOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Salon salon = salonOptional.get();
        salon.getEmployees().clear();
        salonService.saveSalon(salon);

        return ResponseEntity.ok("Svi zaposlenici su uspješno uklonjeni iz salona.");
    }

    // ✅ Upload slika u salon — čuva fajlove na disk, vraća listu path-ova
    @PostMapping("/{id}/upload-images")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> uploadSalonImages(@PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        List<String> savedPaths = salonService.addImagesToSalon(id, files);

        if (!savedPaths.isEmpty()) {
            return ResponseEntity.ok(savedPaths);
        }
        return ResponseEntity.badRequest().body("Greška pri dodavanju slika.");
    }

    // ✅ Dohvati listu path-ova slika salona
    @GetMapping("/{id}/image-paths")
    public ResponseEntity<List<String>> getSalonImagePaths(@PathVariable Long id) {
        List<String> paths = salonService.getSalonImagePaths(id);
        return ResponseEntity.ok(paths);
    }

    // ✅ Brisanje slike iz salona po indexu
    @DeleteMapping("/{salonId}/images/{imageIndex}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> deleteSalonImage(@PathVariable Long salonId, @PathVariable int imageIndex) {
        boolean success = salonService.deleteSalonImage(salonId, imageIndex);

        if (success) {
            return ResponseEntity.ok("Slika uspješno obrisana!");
        }
        return ResponseEntity.badRequest().body("Greška pri brisanju slike.");
    }

    // ✅ Ažuriranje slike u salonu po indexu
    @PutMapping("/{salonId}/images/{imageIndex}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> updateSalonImage(@PathVariable Long salonId, @PathVariable int imageIndex,
            @RequestParam("file") MultipartFile file) {
        String newPath = salonService.updateSalonImage(salonId, imageIndex, file);

        if (newPath != null) {
            return ResponseEntity.ok(newPath);
        }
        return ResponseEntity.badRequest().body("Greška pri ažuriranju slike.");
    }

    @GetMapping("/owner")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> getSalonsByOwner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof String)) {
            System.out.println("❌ Autentifikacija nije uspjela!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Neuspješna autentifikacija.");
        }

        String username = (String) authentication.getPrincipal();
        System.out.println("🔍 Autentifikovani korisnik: " + username);

        List<Salon> salons = salonService.getSalonsByOwnerUsername(username);

        if (salons.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salon nije pronađen za ovog vlasnika.");
        }

        List<Map<String, Object>> salonList = new ArrayList<>();
        for (Salon salon : salons) {
            Map<String, Object> salonMap = new HashMap<>();
            salonMap.put("id", salon.getId());
            salonMap.put("name", salon.getName());
            salonList.add(salonMap);
        }

        return ResponseEntity.ok(salonList);
    }
}
