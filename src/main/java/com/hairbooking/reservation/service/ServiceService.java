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
    private final SalonRepository salonRepository;

    public ServiceService(ServiceRepository serviceRepository, SalonService salonService, SalonRepository salonRepository) {
        this.serviceRepository = serviceRepository;
        this.salonService = salonService;
        this.salonRepository = salonRepository;
    }

    @Transactional
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

    @Transactional
    public String deleteService(Long salonId, Long serviceId) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new EntityNotFoundException("Salon sa ID-em " + salonId + " nije pronađen"));

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Usluga sa ID-em " + serviceId + " nije pronađena"));

        // ❌ Provjera da li usluga pripada salonu
        if (!service.getSalon().getId().equals(salonId)) {
            throw new IllegalArgumentException("Usluga sa ID-em " + serviceId + " ne pripada salonu sa ID-em " + salonId);
        }

        // ✅ Brišemo uslugu iz liste usluga u salonu
        salon.getServices().remove(service);
        serviceRepository.delete(service);

        return "Usluga '" + service.getNazivUsluge() + "' (ID: " + serviceId + ") uspješno obrisana iz salona '" + salon.getName() + "'!";
    }
}
