package com.hairbooking.reservation.service;

import com.hairbooking.reservation.dto.AppointmentHistoryDTO;
import com.hairbooking.reservation.model.*;
import com.hairbooking.reservation.repository.AppointmentRepository;
import com.hairbooking.reservation.repository.CalendarRepository;
import com.hairbooking.reservation.repository.ServiceRepository;
import com.hairbooking.reservation.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Appointment buildAppointment(Long id, Long customerId, AppointmentStatus status, LocalDate date) {
        User customer = new User();
        customer.setId(customerId);
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setPhoneNumber("+987654321");

        ServiceInSalon service = new ServiceInSalon();
        service.setId(2L);
        service.setNazivUsluge("Haircut");
        service.setTrajanjeUsluge(30);
        service.setCijenaUsluge(BigDecimal.valueOf(25.00));

        Salon salon = new Salon();
        salon.setId(5L);
        salon.setName("Beauty Salon XYZ");
        salon.setAddress("123 Main St");
        salon.setPhoneNumber("+123456789");
        service.setSalon(salon);

        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setDate(date);
        appointment.setStartTime(LocalTime.of(14, 30));
        appointment.setEndTime(LocalTime.of(15, 0));
        appointment.setStatus(status);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setCustomer(customer);
        appointment.setService(service);

        return appointment;
    }

    private void mockAuthentication(String username, boolean isAdmin) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        Collection<GrantedAuthority> authorities = isAdmin
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(auth).getAuthorities();

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetUserAppointments_ownAppointments() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("jane");

        mockAuthentication("jane", false);
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(userRepository.existsById(userId)).thenReturn(true);

        Appointment appointment = buildAppointment(10L, userId, AppointmentStatus.CONFIRMED, LocalDate.now().plusDays(5));
        Page<Appointment> appointmentPage = new PageImpl<>(List.of(appointment));
        when(appointmentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(appointmentPage);

        Page<AppointmentHistoryDTO> result = appointmentService.getUserAppointments(userId, null, 0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(AppointmentStatus.CONFIRMED, result.getContent().get(0).getStatus());
    }

    @Test
    void testGetUserAppointments_adminCanAccessAny() {
        Long userId = 2L;

        mockAuthentication("admin", true);
        when(userRepository.existsById(userId)).thenReturn(true);

        Page<Appointment> appointmentPage = new PageImpl<>(Collections.emptyList());
        when(appointmentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(appointmentPage);

        Page<AppointmentHistoryDTO> result = appointmentService.getUserAppointments(userId, null, 0, 10, null);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void testGetUserAppointments_forbiddenForOtherUser() {
        Long requestedUserId = 2L;
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("jane");

        mockAuthentication("jane", false);
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(currentUser));

        assertThrows(AccessDeniedException.class,
                () -> appointmentService.getUserAppointments(requestedUserId, null, 0, 10, null));
    }

    @Test
    void testGetUserAppointments_userNotFound() {
        Long userId = 99L;
        User currentUser = new User();
        currentUser.setId(userId);
        currentUser.setUsername("ghost");

        mockAuthentication("ghost", false);
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.of(currentUser));
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> appointmentService.getUserAppointments(userId, null, 0, 10, null));
    }

    @Test
    void testGetUserAppointments_statusFilterUpcoming() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("jane");

        mockAuthentication("jane", false);
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(userRepository.existsById(userId)).thenReturn(true);

        Appointment upcoming = buildAppointment(10L, userId, AppointmentStatus.CONFIRMED, LocalDate.now().plusDays(3));
        Page<Appointment> appointmentPage = new PageImpl<>(List.of(upcoming));
        when(appointmentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(appointmentPage);

        Page<AppointmentHistoryDTO> result = appointmentService.getUserAppointments(userId, "upcoming", 0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetUserAppointments_statusFilterCancelled() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("jane");

        mockAuthentication("jane", false);
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(userRepository.existsById(userId)).thenReturn(true);

        Appointment cancelled = buildAppointment(11L, userId, AppointmentStatus.CANCELLED, LocalDate.now().minusDays(1));
        Page<Appointment> appointmentPage = new PageImpl<>(List.of(cancelled));
        when(appointmentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(appointmentPage);

        Page<AppointmentHistoryDTO> result = appointmentService.getUserAppointments(userId, "cancelled", 0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(AppointmentStatus.CANCELLED, result.getContent().get(0).getStatus());
    }

    @Test
    void testGetUserAppointments_invalidStatusFilter() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("jane");

        mockAuthentication("jane", false);
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.getUserAppointments(userId, "invalid", 0, 10, null));
    }

    @Test
    void testGetUserAppointments_maxPageSizeEnforced() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("jane");

        mockAuthentication("jane", false);
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(userRepository.existsById(userId)).thenReturn(true);

        Page<Appointment> emptyPage = new PageImpl<>(Collections.emptyList());
        when(appointmentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        appointmentService.getUserAppointments(userId, null, 0, 100, null);

        verify(appointmentRepository).findAll(any(Specification.class), argThat((Pageable pageable) -> pageable.getPageSize() <= 50));
    }

    @Test
    void testGetUserAppointments_combinedStatusFilter() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("jane");

        mockAuthentication("jane", false);
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(userRepository.existsById(userId)).thenReturn(true);

        Page<Appointment> emptyPage = new PageImpl<>(Collections.emptyList());
        when(appointmentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        Page<AppointmentHistoryDTO> result = appointmentService.getUserAppointments(userId, "upcoming,past", 0, 10, null);

        assertNotNull(result);
        verify(appointmentRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }
}
