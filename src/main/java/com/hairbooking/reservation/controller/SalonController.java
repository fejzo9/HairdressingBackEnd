package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.dto.SalonDTO;
import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.service.SalonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/salons")
public class SalonController {

    private final SalonService salonService;

    public SalonController(SalonService salonService) {
        this.salonService = salonService;
    }

    @GetMapping
    public List<SalonDTO> getAllSalons() {
        return salonService.getAllSalons();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Salon> getSalonById(@PathVariable Long id) {
        Optional<Salon> salon = salonService.getSalonById(id);
        return salon.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public Salon createSalon(@RequestBody Salon salon) {
        return salonService.createSalon(salon);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Salon> updateSalon(@PathVariable Long id, @RequestBody Salon salon) {
        Salon updatedSalon = salonService.updateSalon(id, salon);
        return updatedSalon != null ? ResponseEntity.ok(updatedSalon) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSalon(@PathVariable Long id) {
        salonService.deleteSalon(id);
        return ResponseEntity.noContent().build();
    }
}
