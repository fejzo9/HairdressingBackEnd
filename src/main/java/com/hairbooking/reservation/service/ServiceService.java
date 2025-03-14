package com.hairbooking.reservation.service;

import com.hairbooking.reservation.model.ServiceInSalon;
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
    public List<ServiceInSalon> getServicesBySalonId(Long salonId) {
        return serviceRepository.findBySalonId(salonId);
    }

    @Transactional
    public ServiceInSalon addServiceToSalon(Long salonId, ServiceInSalon serviceInSalon) {
        Salon salon = salonService.getSalonById(salonId)
                .orElseThrow(() -> new EntityNotFoundException("Salon nije pronađen")); // ✔️ Koristimo Optional

        serviceInSalon.setSalon(salon);
        return serviceRepository.save(serviceInSalon);
    }

    public ServiceInSalon updateService(Long serviceId, ServiceInSalon newServiceInSalon) {
        ServiceInSalon serviceInSalon = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Usluga nije pronađena"));

        serviceInSalon.setNazivUsluge(newServiceInSalon.getNazivUsluge());
        serviceInSalon.setTrajanjeUsluge(newServiceInSalon.getTrajanjeUsluge());
        serviceInSalon.setCijenaUsluge(newServiceInSalon.getCijenaUsluge());

        return serviceRepository.save(serviceInSalon);
    }

    @Transactional
    public String deleteService(Long salonId, Long serviceId) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new EntityNotFoundException("Salon sa ID-em " + salonId + " nije pronađen"));

        ServiceInSalon serviceInSalon = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Usluga sa ID-em " + serviceId + " nije pronađena"));

        // ❌ Provjera da li usluga pripada salonu
        if (!serviceInSalon.getSalon().getId().equals(salonId)) {
            throw new IllegalArgumentException("Usluga sa ID-em " + serviceId + " ne pripada salonu sa ID-em " + salonId);
        }

        // ✅ Brišemo uslugu iz liste usluga u salonu
        salon.getServices().remove(serviceInSalon);
        serviceRepository.delete(serviceInSalon);

        return "Usluga '" + serviceInSalon.getNazivUsluge() + "' (ID: " + serviceId + ") uspješno obrisana iz salona '" + salon.getName() + "'!";
    }
}
