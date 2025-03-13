package com.hairbooking.reservation.service;

import com.hairbooking.reservation.model.Service;
import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.repository.ServiceRepository;
import com.hairbooking.reservation.repository.SalonRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
public class ServiceService {
    private final ServiceRepository serviceRepository;
    private final SalonService salonService;

    public ServiceService(ServiceRepository serviceRepository, SalonService salonService) {
        this.serviceRepository = serviceRepository;
        this.salonService = salonService;
    }

    public List<Service> getServicesBySalonId(Long salonId) {
        return serviceRepository.findBySalonId(salonId);
    }

    @Transactional
    public Service addServiceToSalon(Long salonId, Service service) {
        Salon salon = salonService.getSalonById(salonId)
                .orElseThrow(() -> new EntityNotFoundException("Salon nije pronađen")); // ✔️ Koristimo Optional

        service.setSalon(salon);
        return serviceRepository.save(service);
    }

    public Service updateService(Long serviceId, Service newService) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Usluga nije pronađena"));

        service.setNazivUsluge(newService.getNazivUsluge());
        service.setTrajanjeUsluge(newService.getTrajanjeUsluge());
        service.setCijenaUsluge(newService.getCijenaUsluge());

        return serviceRepository.save(service);
    }

    public void deleteService(Long serviceId) {
        serviceRepository.deleteById(serviceId);
    }
}
