package com.hairbooking.reservation.service;

import com.hairbooking.reservation.model.*;
import com.hairbooking.reservation.repository.AppointmentRepository;
import com.hairbooking.reservation.repository.CalendarRepository;
import com.hairbooking.reservation.repository.ServiceRepository;
import com.hairbooking.reservation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
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

    private User customer;
    private User hairdresser;
    private User admin;
    private Calendar calendar;
    private ServiceInSalon service;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customer = new User();
        customer.setId(1L);
        customer.setUsername("customer1");
        customer.setRole(Role.USER);

        hairdresser = new User();
        hairdresser.setId(2L);
        hairdresser.setUsername("hairdresser1");
        hairdresser.setRole(Role.HAIRDRESSER);

        admin = new User();
        admin.setId(3L);
        admin.setUsername("admin1");
        admin.setRole(Role.ADMIN);

        calendar = new Calendar();
        calendar.setId(10L);
        calendar.setHairdresser(hairdresser);

        service = new ServiceInSalon();
        service.setId(20L);
        service.setTrajanjeUsluge(60);

        // Appointment far in the future (well beyond 24h window)
        appointment = new Appointment();
        appointment.setId(100L);
        appointment.setCustomer(customer);
        appointment.setCalendar(calendar);
        appointment.setService(service);
        appointment.setDate(LocalDate.now().plusDays(3));
        appointment.setStartTime(LocalTime.of(10, 0));
        appointment.setEndTime(LocalTime.of(11, 0));
        appointment.setStatus(AppointmentStatus.CONFIRMED);
    }

    // ---- cancelAppointment ----

    @Test
    void testCancelAppointmentSuccess_byCustomer() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(customer));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.cancelAppointment(100L, "I changed my mind", "customer1");

        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());
        assertEquals("I changed my mind", result.getCancellationReason());
        assertEquals("customer1", result.getCancelledBy());
        assertNotNull(result.getCancelledAt());
        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    void testCancelAppointmentSuccess_byHairdresser() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByUsername("hairdresser1")).thenReturn(Optional.of(hairdresser));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.cancelAppointment(100L, "Emergency", "hairdresser1");

        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());
        assertEquals("hairdresser1", result.getCancelledBy());
    }

    @Test
    void testCancelAppointmentSuccess_byAdmin() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.cancelAppointment(100L, "Admin override", "admin1");

        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testCancelAppointmentAlreadyCancelled() {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> appointmentService.cancelAppointment(100L, "reason", "customer1"));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void testCancelAppointmentWithin24Hours() {
        // Set appointment to start in 2 hours (within the 24h cancellation window)
        appointment.setDate(LocalDate.now());
        appointment.setStartTime(LocalTime.now().plusHours(2));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(customer));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> appointmentService.cancelAppointment(100L, "reason", "customer1"));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("24 hours"));
    }

    @Test
    void testCancelAppointmentUnauthorized() {
        User stranger = new User();
        stranger.setId(99L);
        stranger.setUsername("stranger");
        stranger.setRole(Role.USER);

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByUsername("stranger")).thenReturn(Optional.of(stranger));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> appointmentService.cancelAppointment(100L, "reason", "stranger"));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void testCancelAppointmentNotFound() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> appointmentService.cancelAppointment(999L, "reason", "customer1"));

        assertEquals(404, ex.getStatusCode().value());
    }

    // ---- rescheduleAppointment ----

    @Test
    void testRescheduleAppointmentSuccess() {
        LocalDate newDate = LocalDate.now().plusDays(5);
        LocalTime newStartTime = LocalTime.of(14, 0);
        LocalTime newEndTime = LocalTime.of(15, 0);

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(customer));
        when(appointmentRepository.existsOverlappingAppointmentExcluding(
                10L, newDate, newStartTime, newEndTime, 100L)).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.rescheduleAppointment(100L, newDate, newStartTime, "customer1");

        assertEquals(AppointmentStatus.RESCHEDULED, result.getStatus());
        assertEquals(newDate, result.getDate());
        assertEquals(newStartTime, result.getStartTime());
        assertEquals(newEndTime, result.getEndTime());
    }

    @Test
    void testRescheduleAppointmentOverlapping() {
        LocalDate newDate = LocalDate.now().plusDays(5);
        LocalTime newStartTime = LocalTime.of(14, 0);
        LocalTime newEndTime = LocalTime.of(15, 0);

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(customer));
        when(appointmentRepository.existsOverlappingAppointmentExcluding(
                10L, newDate, newStartTime, newEndTime, 100L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> appointmentService.rescheduleAppointment(100L, newDate, newStartTime, "customer1"));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("already booked"));
    }

    @Test
    void testRescheduleAppointmentWithin24Hours() {
        appointment.setDate(LocalDate.now());
        appointment.setStartTime(LocalTime.now().plusHours(2));

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(customer));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> appointmentService.rescheduleAppointment(100L, LocalDate.now().plusDays(5), LocalTime.of(10, 0), "customer1"));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void testRescheduleAlreadyCancelledAppointment() {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> appointmentService.rescheduleAppointment(100L, LocalDate.now().plusDays(5), LocalTime.of(10, 0), "customer1"));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void testRescheduleAppointmentUnauthorized() {
        User stranger = new User();
        stranger.setId(99L);
        stranger.setUsername("stranger");
        stranger.setRole(Role.USER);

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByUsername("stranger")).thenReturn(Optional.of(stranger));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> appointmentService.rescheduleAppointment(100L, LocalDate.now().plusDays(5), LocalTime.of(10, 0), "stranger"));

        assertEquals(401, ex.getStatusCode().value());
    }
}
