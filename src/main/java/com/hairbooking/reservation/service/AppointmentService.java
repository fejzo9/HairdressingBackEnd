package com.hairbooking.reservation.service;

import com.hairbooking.reservation.model.Appointment;
import com.hairbooking.reservation.model.Calendar;
import com.hairbooking.reservation.model.ServiceInSalon;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.repository.AppointmentRepository;
import com.hairbooking.reservation.repository.CalendarRepository;
import com.hairbooking.reservation.repository.ServiceRepository;
import com.hairbooking.reservation.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

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
    public void deleteAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Termin nije pronađen"));

        appointmentRepository.delete(appointment);
    }
}
