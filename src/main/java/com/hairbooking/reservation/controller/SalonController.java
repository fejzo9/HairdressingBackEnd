package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.dto.SalonDTO;
import com.hairbooking.reservation.dto.SalonRequestDTO;
import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.service.SalonService;
import com.hairbooking.reservation.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
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

    @Transactional
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
    public ResponseEntity<?> createSalon(@RequestBody SalonRequestDTO request) {
        try {
            Salon newSalon = new Salon();
            newSalon.setName(request.getName());
            newSalon.setAddress(request.getAddress());
            newSalon.setPhoneNumber(request.getPhoneNumber());
            newSalon.setEmail(request.getEmail());

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

    // Deleting Salon
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSalon(@PathVariable Long id) {
        salonService.deleteSalon(id);
        return ResponseEntity.noContent().build();
    }

    // Deleting One Employee from Salon
    @DeleteMapping("/{salonId}/employees/{employeeId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> removeEmployeeFromSalon(@PathVariable Long salonId, @PathVariable Long employeeId) {
        System.out.println("Brisanje zaposlenika ID: " + employeeId + " iz salona ID: " + salonId);

        Optional<Salon> salonOptional = salonService.getSalonById(salonId);
        Optional<User> employeeOptional = userService.findById(employeeId);

        if (salonOptional.isPresent() && employeeOptional.isPresent()) {
            Salon salon = salonOptional.get();
            User employee = employeeOptional.get();

            // ✅ Provjera da li je zaposleni u salonu
            if (salon.getEmployees().contains(employee)) {
                salon.getEmployees().remove(employee);
                salonService.saveSalon(salon); // ✅ Sačuvaj promjene u bazi
                return ResponseEntity.ok("Frizer uspješno uklonjen iz salona.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Frizer nije pronađen u ovom salonu.");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salon ili frizer ne postoje.");
    }

    // Deleting All Employees from Salon
    @DeleteMapping("/{id}/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> removeAllEmployeesFromSalon(@PathVariable Long id) {
        System.out.println("Brisanje svih zaposlenika iz salona ID: " + id);

        Optional<Salon> salonOptional = salonService.getSalonById(id);
        if (salonOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Salon salon = salonOptional.get();
        salon.getEmployees().clear(); // Brišemo sve frizere
        salonService.saveSalon(salon); // Čuvamo promjene

        return ResponseEntity.ok("Svi zaposlenici su uspješno uklonjeni iz salona.");
    }

    // ✅ Upload slika u salon
    @PostMapping("/{id}/upload-images")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> uploadSalonImages(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) {
        boolean success = salonService.addImagesToSalon(id, files);

        if (success) {
            return ResponseEntity.ok("Slike uspješno dodane!");
        }
        return ResponseEntity.badRequest().body("Greška pri dodavanju slika.");
    }

    // ✅ Dohvati sve slike salona
    @GetMapping("/{id}/images")
    public ResponseEntity<List<Map<String, String>>> getSalonImages(@PathVariable Long id) {
        Optional<Salon> salonOptional = salonService.getSalonById(id);

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();

            if (salon.getImages() != null && !salon.getImages().isEmpty()) {
                List<Map<String, String>> imageList = new ArrayList<>();

                for (int i = 0; i < salon.getImages().size(); i++) {
                    Map<String, String> imageMap = new HashMap<>();
                    imageMap.put("imageData", Base64.getEncoder().encodeToString(salon.getImages().get(i))); // ✅ Enkodiraj u Base64
                    imageMap.put("contentType", salon.getImageTypes().get(i)); // ✅ Dodaj format slike (jpeg/png)
                    imageList.add(imageMap);
                }

                return ResponseEntity.ok(imageList);
            }
        }

        return ResponseEntity.notFound().build();
    }

    // Dohvati jednu sliku salona
    @GetMapping("/{id}/images/{imageIndex}")
    public ResponseEntity<byte[]> getSalonImage(@PathVariable Long id, @PathVariable int imageIndex) {
        Optional<Salon> salonOptional = salonService.getSalonById(id);

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();

            if (salon.getImages() != null && imageIndex < salon.getImages().size()) {
                byte[] imageBytes = salon.getImages().get(imageIndex);
                String contentType = salon.getImageTypes().get(imageIndex);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));

                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            }
        }

        return ResponseEntity.notFound().build();
    }

    // ✅ Brisanje slike iz salona
    @DeleteMapping("/{salonId}/images/{imageIndex}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> deleteSalonImage(@PathVariable Long salonId, @PathVariable int imageIndex) {
        boolean success = salonService.deleteSalonImage(salonId, imageIndex);

        if (success) {
            return ResponseEntity.ok("Slika uspješno obrisana!");
        }
        return ResponseEntity.badRequest().body("Greška pri brisanju slike.");
    }

    // ✅ Ažuriranje slike u salonu
    @PutMapping("/{salonId}/images/{imageIndex}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> updateSalonImage(@PathVariable Long salonId, @PathVariable int imageIndex, @RequestParam("file") MultipartFile file) {
        boolean success = salonService.updateSalonImage(salonId, imageIndex, file);

        if (success) {
            return ResponseEntity.ok("Slika uspješno ažurirana!");
        }
        return ResponseEntity.badRequest().body("Greška pri ažuriranju slike.");
    }
}
