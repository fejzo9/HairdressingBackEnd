package com.hairbooking.reservation.service;

import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
    testCreateUserSuccess: Testira uspješno kreiranje korisnika.
    testCreateUserDuplicateUsername: Provjerava da li aplikacija pravilno hendluje duplicirano korisničko ime.
    testUpdateUserSuccess: Testira uspješno ažuriranje korisnika.
    testUpdateUserNotFound: Provjerava ponašanje kada korisnik za ažuriranje ne postoji u bazi.
    testVerifyPassword: Osigurava da se fino enktiptuje pw
 */
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUserSuccess() {
        LocalDate date = LocalDate.of(1999, 8, 22);
        User user = new User("Amar", "Mujanović", "mujke333@gmail.com","amko333", "amkomujan3377", "male", "+38761224366", date );

        when(userRepository.findByUsername(user.getUsername())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals("Amar", createdUser.getFirstName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUserDuplicateUsername() {
        LocalDate date = LocalDate.of(1999, 8, 22);
        User user = new User("Amar", "Mujanović", "mujke333@gmail.com","amko333", "amkomujan3377", "male", "+38761224366", date );
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));

        assertEquals("Username already exists!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUserSuccess() {
        LocalDate date = LocalDate.of(1999, 8, 22);
        User existingUser =  new User("Amar", "Mujanović", "mujke333@gmail.com","amko333", "amkomujan3377", "male", "+38761224366", date );
        User updatedUser = new User("Amar", "Mujanović", "mujke333@gmail.com","amko333", "novipvpw33", "male", "+38761224366", date );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUser(1L, updatedUser);

        assertNotNull(result);
        assertEquals("Mujanović", result.getLastName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        LocalDate date = LocalDate.of(1999, 8, 22);
        User updatedUser =  new User("Amar", "Mujanović", "mujke333@gmail.com","amko333", "amkomujan3377", "male", "+38761224366", date );

        User result = userService.updateUser(1L, updatedUser);

        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testVerifyPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password123";
        String encodedPassword = encoder.encode(rawPassword);

        boolean isMatched = encoder.matches(rawPassword, encodedPassword);
        assertTrue(isMatched, "Password verification failed");
    }

}

