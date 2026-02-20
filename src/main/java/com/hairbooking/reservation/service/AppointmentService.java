package com.hairbooking.reservation.service;

import com.hairbooking.reservation.dto.AppointmentHistoryDTO;
import com.hairbooking.reservation.model.Appointment;
import com.hairbooking.reservation.model.AppointmentStatus;
import com.hairbooking.reservation.model.Calendar;
import com.hairbooking.reservation.model.Role;
import com.hairbooking.reservation.model.ServiceInSalon;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.repository.AppointmentRepository;
import com.hairbooking.reservation.repository.AppointmentSpecification;
import com.hairbooking.reservation.repository.CalendarRepository;
import com.hairbooking.reservation.repository.ServiceRepository;
import com.hairbooking.reservation.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Service
public class AppointmentService {

    private static final long CANCELLATION_WINDOW_HOURS = 24;

    private final AppointmentRepository appointmentRepository;
    private final CalendarRepository calendarRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository, CalendarRepository calendarRepository, ServiceRepository serviceRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.calendarRepository = calendarRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Appointment bookAppointment(Long calendarId, Long serviceId, Long customerId, LocalDate date, LocalTime startTime) {
        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new EntityNotFoundException("Kalendar nije pronađen"));

        ServiceInSalon service =  serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Usluga nije pronađena"));

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Korisnik nije pronađen"));

        LocalTime endTime = startTime.plusMinutes(service.getTrajanjeUsluge());

        // ✅ Provjera da li postoji već rezervisan termin u tom vremenskom rasponu
        boolean isOverlapping = appointmentRepository.findByCalendarId(calendarId).stream()
                .anyMatch(existingAppointment -> // Prolazi kroz sve termine u kalendaru i provjerava da li neki od njih preklapa novi termin
                        existingAppointment.getDate().equals(date) &&
                                ((startTime.isBefore(existingAppointment.getEndTime()) && startTime.isAfter(existingAppointment.getStartTime())) || // Provjerava da li novi termin počinje unutar postojećeg termina npr. postojeći termin od 10-11h, novi termin 10:30 do 11:30h
                                        (endTime.isAfter(existingAppointment.getStartTime()) && endTime.isBefore(existingAppointment.getEndTime())) || // Provjerava da li novi termin završava unutar postojećeg termina npr. postojeći termin od 10-11h, novi termin 9:30 do 10:30h
                                        (startTime.equals(existingAppointment.getStartTime()) || endTime.equals(existingAppointment.getEndTime())) || // Provjerava da li novi termin počinje ili završava tačno u isto vrijeme kao postojeći termin npr. postojeći termin od 10-11h, novi termin: 10-10:30h ili 9:30 do 11h
                                        (startTime.isBefore(existingAppointment.getStartTime()) && endTime.isAfter(existingAppointment.getEndTime()))) // Provjerava da li novi termin potpuno obuhvata neki postojeći termin npr. postojeći termin 10-11h, novi termin 9:30 do 11:30h
                );

        if (isOverlapping) {
            throw new IllegalStateException("Termin u odabranom vremenu je već zauzet!");
        }

        Appointment appointment = new Appointment();
        appointment.setCalendar(calendar);
        appointment.setService(service);
        appointment.setCustomer(customer);
        appointment.setDate(date);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setCreatedAt(LocalDateTime.now());

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public List<Appointment> getAppointmentsByCalendar(Long calendarId) {
        return appointmentRepository.findByCalendarId(calendarId);
    }

    @Transactional
    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Termin nije pronađen"));
    }

    @Transactional
    public Appointment updateAppointment(Long appointmentId, LocalDate date, LocalTime startTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Termin nije pronađen"));

        LocalTime endTime = startTime.plusMinutes(appointment.getService().getTrajanjeUsluge());

        appointment.setDate(date);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void deleteAppointment(Long appointmentId, String username) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Termin nije pronađen"));

        checkAuthorization(appointment, username);

        appointmentRepository.delete(appointment);
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId, String reason, String username) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment is already cancelled");
        }

        checkAuthorization(appointment, username);
        checkNotWithinCancellationWindow(appointment);

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason);
        appointment.setCancelledAt(LocalDateTime.now());
        appointment.setCancelledBy(username);

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment rescheduleAppointment(Long appointmentId, LocalDate newDate, LocalTime newStartTime, String username) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot reschedule a cancelled appointment");
        }

        checkAuthorization(appointment, username);
        checkNotWithinCancellationWindow(appointment);

        if (newDate == null || newStartTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New date and start time are required");
        }

        LocalTime newEndTime = newStartTime.plusMinutes(appointment.getService().getTrajanjeUsluge());

        boolean isOverlapping = appointmentRepository.existsOverlappingAppointmentExcluding(
                appointment.getCalendar().getId(), newDate, newStartTime, newEndTime, appointmentId);

        if (isOverlapping) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested time slot is already booked");
        }

        appointment.setDate(newDate);
        appointment.setStartTime(newStartTime);
        appointment.setEndTime(newEndTime);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Page<AppointmentHistoryDTO> getUserAppointments(Long userId, String status, int page, int size, String sort) {
        // Enforce max page size
        size = Math.min(size, 50);

        // Authorization check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isAdmin) {
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new EntityNotFoundException("Korisnik nije pronađen"));
            if (!currentUser.getId().equals(userId)) {
                throw new AccessDeniedException("Pristup nije dozvoljen");
            }
        }

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Korisnik nije pronađen");
        }

        // Build sort
        Sort sortOrder = buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // Build specification
        Specification<Appointment> spec = Specification.where(AppointmentSpecification.hasCustomerId(userId));

        if (status != null && !status.isBlank()) {
            List<String> filters = Arrays.asList(status.split(","));
            Specification<Appointment> statusSpec = null;
            LocalDate today = LocalDate.now();

            for (String filter : filters) {
                Specification<Appointment> filterSpec = switch (filter.trim().toLowerCase()) {
                    case "upcoming" -> AppointmentSpecification.isUpcoming(today);
                    case "past" -> AppointmentSpecification.isPast(today);
                    case "cancelled" -> AppointmentSpecification.isCancelled();
                    default -> throw new IllegalArgumentException("Nevalidan status filter: " + filter.trim());
                };
                statusSpec = (statusSpec == null) ? filterSpec : statusSpec.or(filterSpec);
            }

            spec = spec.and(statusSpec);
        }

        return appointmentRepository.findAll(spec, pageable)
                .map(AppointmentHistoryDTO::new);
    }

    private void checkAuthorization(Appointment appointment, String username) {
        User caller = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isCustomer = appointment.getCustomer().getUsername().equals(username);
        boolean isHairdresser = appointment.getCalendar().getHairdresser().getUsername().equals(username);
        boolean isPrivileged = caller.getRole() == Role.ADMIN
                || caller.getRole() == Role.SUPER_ADMIN
                || caller.getRole() == Role.OWNER;

        if (!isCustomer && !isHairdresser && !isPrivileged) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to modify this appointment");
        }
    }

    private Sort buildSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "date");
        }
        boolean descending = sort.startsWith("-");
        String field = descending ? sort.substring(1) : sort;
        Sort.Direction direction = descending ? Sort.Direction.DESC : Sort.Direction.ASC;

        return switch (field.toLowerCase()) {
            case "date" -> Sort.by(direction, "date");
            case "salon" -> Sort.by(direction, "service.salon.name");
            case "status" -> Sort.by(direction, "status");
            default -> Sort.by(Sort.Direction.DESC, "date");
        };
    }

    private void checkNotWithinCancellationWindow(Appointment appointment) {
        LocalDateTime appointmentDateTime = LocalDateTime.of(appointment.getDate(), appointment.getStartTime());
        if (LocalDateTime.now().plusHours(CANCELLATION_WINDOW_HOURS).isAfter(appointmentDateTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot cancel or reschedule within " + CANCELLATION_WINDOW_HOURS + " hours of the appointment");
        }
    }
}

