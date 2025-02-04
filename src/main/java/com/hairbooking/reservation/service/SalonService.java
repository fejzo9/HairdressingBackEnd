package com.hairbooking.reservation.service;

import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.repository.SalonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalonService {

    private final SalonRepository salonRepository;

    public SalonService(SalonRepository salonRepository) {
        this.salonRepository = salonRepository;
    }

    public List<Salon> getAllSalons() {
        return salonRepository.findAll();
    }

    public Optional<Salon> getSalonById(Long id) {
        return salonRepository.findById(id);
    }

    public Salon createSalon(Salon salon) {
        return salonRepository.save(salon);
    }

    public Salon updateSalon(Long id, Salon updatedSalon) {
        return salonRepository.findById(id).map(existingSalon -> {
            existingSalon.setName(updatedSalon.getName());
            existingSalon.setAddress(updatedSalon.getAddress());
            existingSalon.setPhoneNumber(updatedSalon.getPhoneNumber());
            existingSalon.setEmail(updatedSalon.getEmail());
            existingSalon.setEmployees(updatedSalon.getEmployees());
            return salonRepository.save(existingSalon);
        }).orElse(null);
    }

    public void deleteSalon(Long id) {
        salonRepository.deleteById(id);
    }
}
