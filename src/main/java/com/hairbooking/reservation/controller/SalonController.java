package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.dto.SalonDTO;
import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.service.SalonService;
import com.hairbooking.reservation.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @GetMapping("/{id}")
    public ResponseEntity<SalonDTO> getSalonById(@PathVariable Long id) {
        System.out.println("Zahtjev za salon sa ID-jem: " + id);
        Optional<Salon> salonOptional = salonService.getSalonById(id);

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();
            User owner = salon.getOwner(); // Dohvati vlasnika

            SalonDTO salonDTO = new SalonDTO(
                    salon.getId(),
                    salon.getName(),
                    salon.getAddress(),
                    salon.getPhoneNumber(),
                    salon.getEmail(),
                    salon.getEmployees() != null
                            ? salon.getEmployees().stream().map(User::getUsername).collect(Collectors.toList())
                            : List.of(), // Ako nema zaposlenih, vrati praznu listu
                    owner != null ? owner.getId() : null, // ID vlasnika
                    owner != null ? owner.getFirstName() : "N/A", // Ime vlasnika
                    owner != null ? owner.getLastName() : "N/A", // Prezime vlasnika
                    owner != null ? String.valueOf(owner.getBirthDate()) : "N/A", // Datum rođenja vlasnika
                    owner != null ? owner.getEmail() : "N/A", // Email vlasnika
                    owner != null ? owner.getPhoneNumber() : "N/A", // Broj telefona vlasnika
                    owner != null ? owner.getUsername() : "N/A" // Username vlasnika
            );

            System.out.println("Salon pronađen: " + salon.getName());
            return ResponseEntity.ok(salonDTO);
        } else {
            System.out.println("Salon nije pronađen!");
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Salon createSalon(@RequestBody Salon salon) {
        return salonService.createSalon(salon);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Salon> updateSalon(@PathVariable Long id, @RequestBody Salon salon) {
        Salon updatedSalon = salonService.updateSalon(id, salon);
        return updatedSalon != null ? ResponseEntity.ok(updatedSalon) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/employees")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SalonDTO> updateSalonEmployees(@PathVariable Long id, @RequestBody List<Long> employeeIds) {
        System.out.println("Dodavanje zaposlenika u salon ID: " + id);
        Optional<Salon> salonOptional = salonService.getSalonById(id);

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();
            List<User> employees = userService.findUsersByIds(employeeIds); // ✅ Dohvati korisnike po ID-ju
            salon.setEmployees(employees);
            salonService.saveSalon(salon); // ✅ Sačuvaj promjene u bazi

            // ✅ Kreiraj DTO odgovor
            SalonDTO salonDTO = new SalonDTO(
                    salon.getId(),
                    salon.getName(),
                    salon.getAddress(),
                    salon.getPhoneNumber(),
                    salon.getEmail(),
                    salon.getEmployees().stream().map(User::getUsername).collect(Collectors.toList()),
                    salon.getOwner().getId(),
                    salon.getOwner().getFirstName(),
                    salon.getOwner().getLastName(),
                    String.valueOf(salon.getOwner().getBirthDate()),
                    salon.getOwner().getEmail(),
                    salon.getOwner().getPhoneNumber(),
                    salon.getOwner().getUsername()
            );

            return ResponseEntity.ok(salonDTO);
        }
        return ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSalon(@PathVariable Long id) {
        salonService.deleteSalon(id);
        return ResponseEntity.noContent().build();
    }
}
