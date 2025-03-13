package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.dto.ServiceDTO;
import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.model.Service;
import com.hairbooking.reservation.service.SalonService;
import com.hairbooking.reservation.service.ServiceService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {
    private final ServiceService serviceService;
    private final SalonService salonService;

    public ServiceController(ServiceService serviceService, SalonService salonService) {
        this.serviceService = serviceService;
        this.salonService = salonService;
    }

    @GetMapping("/salon/{salonId}")
    @PreAuthorize("permitAll()") // Svi mogu vidjeti usluge salona
    public ResponseEntity<List<Service>> getServicesBySalon(@PathVariable Long salonId) {
        return ResponseEntity.ok(serviceService.getServicesBySalonId(salonId));
    }

    @PostMapping("/salon/{salonId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ServiceDTO> addService(@PathVariable Long salonId, @RequestBody ServiceDTO serviceDTO) {
        Salon salon = salonService.getSalonById(salonId)
                .orElseThrow(() -> new EntityNotFoundException("Salon nije pronađen")); // ✔️ Koristimo Optional

        Service newService = serviceService.addServiceToSalon(salon.getId(), serviceDTO.toEntity(salon));

        return ResponseEntity.ok(new ServiceDTO(newService));
    }


    @PutMapping("/{serviceId}")
    public ResponseEntity<Service> updateService(@PathVariable Long serviceId, @RequestBody Service service) {
        return ResponseEntity.ok(serviceService.updateService(serviceId, service));
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {
        serviceService.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }
}
