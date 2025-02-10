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

    @PatchMapping("/{id}/employees/add")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SalonDTO> addEmployeeToSalon(@PathVariable Long id, @RequestBody Long employeeId) {
        System.out.println("Dodavanje frizera sa ID: " + employeeId + " u salon ID: " + id);

        Optional<Salon> salonOptional = salonService.getSalonById(id);
        Optional<User> employeeOptional = Optional.ofNullable(userService.getUserById(employeeId));

        if (salonOptional.isPresent() && employeeOptional.isPresent()) {
            Salon salon = salonOptional.get();
            User employee = employeeOptional.get();

            // ✅ Ako frizer nije već u salonu, dodaj ga
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
        List<User> existingEmployees = salon.getEmployees(); // Trenutni frizeri u salonu
        List<User> newEmployees = userService.findUsersByIds(employeeIds); // Novi frizeri iz request body-a

        // ✅ Dodaj nove frizere koji još nisu u listi
        for (User newEmployee : newEmployees) {
            if (!existingEmployees.contains(newEmployee)) {
                existingEmployees.add(newEmployee);
            }
        }
        salon.setEmployees(existingEmployees);

        salonService.saveSalon(salon); // ✅ Sačuvaj izmjene

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
                salon.getOwner().getBirthDate() != null ? salon.getOwner().getBirthDate().toString() : "N/A",
                salon.getOwner().getEmail(),
                salon.getOwner().getPhoneNumber(),
                salon.getOwner().getUsername()
        );

        return ResponseEntity.ok(salonDTO);
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

    @DeleteMapping("/{id}/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> removeAllEmployeesFromSalon(@PathVariable Long id) {
        System.out.println("Brisanje svih zaposlenika iz salona ID: " + id);

        Optional<Salon> salonOptional = salonService.getSalonById(id);
        if (salonOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Salon salon = salonOptional.get();
        salon.getEmployees().clear(); // ✅ Brišemo sve frizere
        salonService.saveSalon(salon); // ✅ Sačuvaj promjene

        return ResponseEntity.ok("Svi zaposlenici su uspješno uklonjeni iz salona.");
    }

}
