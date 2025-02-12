package com.hairbooking.reservation.service;

import com.hairbooking.reservation.dto.SalonDTO;
import com.hairbooking.reservation.dto.SalonImageDTO;
import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.repository.SalonRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.hairbooking.reservation.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    // Čuvanje salona
    @Transactional
    public Salon saveSalon(Salon salon) {
        return salonRepository.save(salon);
    }

    // Brisanje salona
    public void deleteSalon(Long id) {
        salonRepository.deleteById(id);
    }

    // Dodavanje slika u salon
    public boolean addImagesToSalon(Long salonId, List<MultipartFile> files) {
        Optional<Salon> salonOptional = salonRepository.findById(salonId);

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();

            try {
                // ✅ Ako je lista `images` null, inicijalizuje je
                if (salon.getImages() == null) {
                    salon.setImages(new ArrayList<>());
                }
                if (salon.getImageTypes() == null) {
                    salon.setImageTypes(new ArrayList<>());
                }
                for (MultipartFile file : files) {
                    salon.getImages().add(file.getBytes());
                    salon.getImageTypes().add(file.getContentType());
                }
                salonRepository.save(salon);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // ✅ Dohvati slike salona
    public List<SalonImageDTO> getSalonImages(Long salonId) {
        Optional<Salon> salonOptional = salonRepository.findById(salonId);

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();
            List<SalonImageDTO> images = new ArrayList<>();

            for (int i = 0; i < salon.getImages().size(); i++) {
                images.add(new SalonImageDTO(salon.getImages().get(i), salon.getImageTypes().get(i)));
            }
            return images;
        }
        return Collections.emptyList();
    }

    // ✅ Brisanje određene slike po indexu
    public boolean deleteSalonImage(Long salonId, int imageIndex) {
        Optional<Salon> salonOptional = salonRepository.findById(salonId);

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();

            if (imageIndex >= 0 && imageIndex < salon.getImages().size()) {
                salon.getImages().remove(imageIndex);
                salon.getImageTypes().remove(imageIndex);
                salonRepository.save(salon);
                return true;
            }
        }
        return false;
    }

    // ✅ Update određene slike
    public boolean updateSalonImage(Long salonId, int imageIndex, MultipartFile newFile) {
        Optional<Salon> salonOptional = salonRepository.findById(salonId);

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();

            if (imageIndex >= 0 && imageIndex < salon.getImages().size()) {
                try {
                    salon.getImages().set(imageIndex, newFile.getBytes());
                    salon.getImageTypes().set(imageIndex, newFile.getContentType());
                    salonRepository.save(salon);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
