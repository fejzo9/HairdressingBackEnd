package com.hairbooking.reservation.service;

import com.hairbooking.reservation.dto.UserDTO;
import com.hairbooking.reservation.model.Role;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CalendarService calendarService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public UserService(UserRepository userRepository, CalendarService calendarService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.calendarService = calendarService;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public Optional<User> getUserByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsernameWithCalendar(username);

        userOptional.ifPresent(user -> {
            System.out.println("🛠️ Učitani korisnik: " + user.getUsername());
            System.out.println(
                    "📅 Calendar ID: " + (user.getCalendar() != null ? user.getCalendar().getId() : "Nema kalendara"));
        });

        return userOptional;
    }

    public Optional<User> getUserByIdOptional(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

    public UserDTO getUserDTOById(Long id) {
        return userRepository.findById(id)
                .map(UserDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Korisnik nije pronađen"));
    }

    public User createUser(User user) {

        // Provjera da li su sva polja popunjena
        if (user.getFirstName() == null || user.getLastName() == null ||
                user.getEmail() == null || user.getUsername() == null ||
                user.getPassword() == null || user.getBirthDate() == null ||
                user.getGender() == null || user.getPhoneNumber() == null) {
            throw new IllegalArgumentException("All fields are required!");
        }

        // Provjera formata e-mail adrese
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format!");
        }

        // Provjera formata broja telefona (npr. +38761266355)
        if (!user.getPhoneNumber().matches("^\\+387(6\\d|3\\d)\\d{6,7}$")) {
            throw new IllegalArgumentException("Invalid phone number format! Example: +38761266355");
        }

        // Provjera za polje gender (dozvoljeno: male, female, other)
        if (!user.getGender().matches("^(male|female|other)$")) {
            throw new IllegalArgumentException("Invalid gender! Allowed values are: male, female, other");
        }

        // Provjera formata datuma
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        try {
            String formattedDate = user.getBirthDate().format(formatter);
            String formattedDate2 = user.getBirthDate().format(formatter2);
            if (!formattedDate.matches("^\\d{2}/\\d{2}/\\d{4}$") || !formattedDate2.matches("^\\d{2}.\\d{2}.\\d{4}$")) {
                throw new IllegalArgumentException("Invalid date format! Use dd-MM-yyyy.");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format! Use dd-MM-yyyy.");
        }

        // Provjera da li korisnik sa istim e-mailom ili korisničkim imenom već postoji
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists!");
        }
        if (userRepository.findAll().stream()
                .anyMatch(existingUser -> existingUser.getEmail().equals(user.getEmail()))) {
            throw new IllegalArgumentException("Email already exists!");
        }

        // Provjera dužine lozinke
        if (user.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long!");
        }

        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id).orElse(null);

        if (user != null) {

            if (updatedUser.getFirstName() != null) {
                user.setFirstName(updatedUser.getFirstName());
            }

            if (updatedUser.getLastName() != null) {
                user.setLastName(updatedUser.getLastName());
            }

            if (updatedUser.getEmail() != null) {
                if (!updatedUser.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    throw new IllegalArgumentException("Invalid email format!");
                }
                user.setEmail(updatedUser.getEmail());
            }

            if (updatedUser.getUsername() != null) {
                user.setUsername(updatedUser.getUsername());
            }

            if (updatedUser.getPassword() != null) {
                if (updatedUser.getPassword().length() < 8) {
                    throw new IllegalArgumentException("Password must be at least 8 characters long!");
                }
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            if (updatedUser.getPhoneNumber() != null && user.getPhoneNumber().matches("^\\+387(6\\d|3\\d)\\d{6,7}$")) {
                user.setPhoneNumber(updatedUser.getPhoneNumber());
            }

            if (updatedUser.getGender() != null && user.getGender().matches("^(male|female|other)$")) {
                user.setGender(updatedUser.getGender());
            }

            if (updatedUser.getBirthDate() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                try {
                    String formattedDate = user.getBirthDate().format(formatter);
                    if (formattedDate.matches("^\\d{2}-\\d{2}-\\d{4}$")) {
                        user.setBirthDate(updatedUser.getBirthDate());
                    }
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid date format! Use dd-MM-yyyy.");
                }
            }

            if (updatedUser.getRole() != null) {
                user.setRole(updatedUser.getRole());
            }

            return userRepository.save(user);
        }
        return null;
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Pronalaženje korisnika na osnovu korisničkog imena ili emaila
    @Transactional
    public User findByUsernameOrEmail(String identifier) {
        Optional<User> user = userRepository.findByUsername(identifier);

        return user.orElseGet(() -> user.orElseGet(() -> userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(identifier))
                .findFirst()
                .orElse(null)));
    }

    // Verifikacija lozinke
    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // Dohvatanje korisnika na osnovu uloge
    @Transactional
    public List<User> getUsersByRole(Role role) {
        return userRepository.findAllByRole(role);
    }

    // Promjena passworda
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return false;
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // ✅ Upload profilne slike — čuva fajl na disk, u bazi samo path
    public String uploadProfilePicture(Long userId, MultipartFile file) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Kreiramo direktorij ako ne postoji
                Path profileDir = Paths.get(uploadDir, "profile");
                Files.createDirectories(profileDir);

                // Generišemo jedinstveno ime fajla
                String originalFilename = file.getOriginalFilename();
                String extension = (originalFilename != null && originalFilename.contains("."))
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".jpg";
                String filename = "user_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

                // Snimamo fajl na disk
                Path filePath = profileDir.resolve(filename);
                Files.write(filePath, file.getBytes());

                // Čuvamo relativni path u bazi
                String relativePath = "/uploads/profile/" + filename;
                user.setProfilePicturePath(relativePath);
                userRepository.save(user);

                return relativePath;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅ Dohvati path profilne slike korisnika
    public String getProfilePicturePath(Long userId) {
        return userRepository.findById(userId)
                .map(User::getProfilePicturePath)
                .orElse(null);
    }

    @Transactional
    public User registerHairdresser(User hairdresser) {
        if (!Role.HAIRDRESSER.equals(hairdresser.getRole())) {
            throw new IllegalArgumentException("Samo frizer može biti registrovan kao frizer!");
        }

        User savedHairdresser = userRepository.save(hairdresser);
        calendarService.createCalendarForHairdresser(savedHairdresser.getId());

        return savedHairdresser;
    }
}