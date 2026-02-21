package com.hairbooking.reservation.service;

import com.hairbooking.reservation.dto.SalonDTO;
import com.hairbooking.reservation.model.Salon;
import com.hairbooking.reservation.repository.SalonRepository;
import com.hairbooking.reservation.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.hairbooking.reservation.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SalonService {

    private final SalonRepository salonRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public SalonService(SalonRepository salonRepository, UserRepository userRepository) {
        this.salonRepository = salonRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<SalonDTO> getAllSalons() {
        return salonRepository.findAll().stream()
                .map(SalonDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<Salon> getSalonById(Long id) {
        Optional<Salon> salonOptional = salonRepository.findById(id);
        salonOptional.ifPresent(salon -> {
            salon.getEmployees().size();
            salon.getServices().size();
        });
        return salonOptional;
    }

    @Transactional
    public Optional<Salon> getEmployeesForSalon(Long salonId) {
        Optional<Salon> salonOptional = salonRepository.findById(salonId);
        salonOptional.ifPresent(salon -> salon.getEmployees().size());
        return salonOptional;
    }

    @Transactional
    public Optional<Salon> getServicesForSalon(Long salonId) {
        Optional<Salon> salonOptional = salonRepository.findById(salonId);
        salonOptional.ifPresent(salon -> salon.getServices().size());
        return salonOptional;
    }

    @Transactional
    public Salon createSalon(Salon salon, String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new IllegalArgumentException(
                        "❌ Vlasnik sa username-om '" + ownerUsername + "' nije pronađen!"));

        salon.setOwner(owner);
        return salonRepository.save(salon);
    }

    public Salon updateSalon(Long id, Salon updatedSalon) {
        return salonRepository.findById(id).map(existingSalon -> {
            existingSalon.setName(updatedSalon.getName());
            existingSalon.setAddress(updatedSalon.getAddress());
            existingSalon.setPhoneNumber(updatedSalon.getPhoneNumber());
            existingSalon.setEmail(updatedSalon.getEmail());
            existingSalon.setEmployees(updatedSalon.getEmployees());
            existingSalon.setLatitude(updatedSalon.getLatitude());
            existingSalon.setLongitude(updatedSalon.getLongitude());
            return salonRepository.save(existingSalon);
        }).orElse(null);
    }

    @Transactional
    public Salon saveSalon(Salon salon) {
        return salonRepository.save(salon);
    }

    public void deleteSalon(Long id) {
        salonRepository.deleteById(id);
    }

    // ✅ Dodavanje slika u salon — čuva fajlove na disk, u bazi samo path-ovi
    public List<String> addImagesToSalon(Long salonId, List<MultipartFile> files) {
        Optional<Salon> salonOptional = salonRepository.findById(salonId);
        List<String> savedPaths = new ArrayList<>();

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();

            try {
                Path salonDir = Paths.get(uploadDir, "salons", String.valueOf(salonId));
                Files.createDirectories(salonDir);

                for (MultipartFile file : files) {
                    String originalFilename = file.getOriginalFilename();
                    String extension = (originalFilename != null && originalFilename.contains("."))
                            ? originalFilename.substring(originalFilename.lastIndexOf("."))
                            : ".jpg";
                    String filename = "salon_" + salonId + "_" + UUID.randomUUID().toString().substring(0, 8)
                            + extension;

                    Path filePath = salonDir.resolve(filename);
                    Files.write(filePath, file.getBytes());

                    String relativePath = "/uploads/salons/" + salonId + "/" + filename;
                    salon.getImagePaths().add(relativePath);
                    savedPaths.add(relativePath);
                }
                salonRepository.save(salon);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return savedPaths;
    }

    // ✅ Dohvati listu path-ova slika salona
    public List<String> getSalonImagePaths(Long salonId) {
        return salonRepository.findById(salonId)
                .map(Salon::getImagePaths)
                .orElse(new ArrayList<>());
    }

    // ✅ Brisanje određene slike po indexu — briše fajl s diska i path iz baze
    public boolean deleteSalonImage(Long salonId, int imageIndex) {
        Optional<Salon> salonOptional = salonRepository.findById(salonId);

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();

            if (imageIndex >= 0 && imageIndex < salon.getImagePaths().size()) {
                String imagePath = salon.getImagePaths().get(imageIndex);

                // Pokušaj obrisati fajl s diska
                try {
                    // imagePath je npr. "/uploads/salons/1/salon_1_abc.jpg"
                    Path filePath = Paths.get(uploadDir, imagePath.replace("/uploads/", ""));
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                salon.getImagePaths().remove(imageIndex);
                salonRepository.save(salon);
                return true;
            }
        }
        return false;
    }

    // ✅ Ažuriranje slike po indexu — zamjena fajla na disku i path-a u bazi
    public String updateSalonImage(Long salonId, int imageIndex, MultipartFile newFile) {
        Optional<Salon> salonOptional = salonRepository.findById(salonId);

        if (salonOptional.isPresent()) {
            Salon salon = salonOptional.get();

            if (salon.getImagePaths() != null && imageIndex >= 0 && imageIndex < salon.getImagePaths().size()) {
                try {
                    // Obrišemo stari fajl
                    String oldPath = salon.getImagePaths().get(imageIndex);
                    Path oldFilePath = Paths.get(uploadDir, oldPath.replace("/uploads/", ""));
                    Files.deleteIfExists(oldFilePath);

                    // Snimimo novi fajl
                    Path salonDir = Paths.get(uploadDir, "salons", String.valueOf(salonId));
                    Files.createDirectories(salonDir);

                    String originalFilename = newFile.getOriginalFilename();
                    String extension = (originalFilename != null && originalFilename.contains("."))
                            ? originalFilename.substring(originalFilename.lastIndexOf("."))
                            : ".jpg";
                    String filename = "salon_" + salonId + "_" + UUID.randomUUID().toString().substring(0, 8)
                            + extension;

                    Path newFilePath = salonDir.resolve(filename);
                    Files.write(newFilePath, newFile.getBytes());

                    String newRelativePath = "/uploads/salons/" + salonId + "/" + filename;
                    salon.getImagePaths().set(imageIndex, newRelativePath);
                    salonRepository.save(salon);
                    return newRelativePath;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public List<Salon> getSalonByOwnerId(Long ownerId) {
        return salonRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public List<Salon> getSalonsByOwnerUsername(String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Vlasnik sa username-om " + username + " nije pronađen"));
        return salonRepository.findByOwnerId(owner.getId());
    }
}
