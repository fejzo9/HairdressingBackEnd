package com.hairbooking.reservation.service;

import com.hairbooking.reservation.dto.SalonDTO;
import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.repository.SalonRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.hairbooking.reservation.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SalonService {

    private final SalonRepository salonRepository;

    public SalonService(SalonRepository salonRepository) {
        this.salonRepository = salonRepository;
    }

    @Transactional
    public List<SalonDTO> getAllSalons() {
        return salonRepository.findAll().stream().map(salon -> {
            User owner = salon.getOwner();
               return new SalonDTO(
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
    }).collect(Collectors.toList());
    }

    @Transactional
    public Optional<Salon> getSalonById(Long id) {
        Optional<Salon> salonOptional = salonRepository.findById(id);
        salonOptional.ifPresent(salon -> {
            salon.getEmployees().size(); // Prisilno učitavanje zaposlenih (Lazy Loading)
        });
        return salonOptional;
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
