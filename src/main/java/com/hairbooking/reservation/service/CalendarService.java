package com.hairbooking.reservation.service;

import com.hairbooking.reservation.model.Calendar;
import com.hairbooking.reservation.model.Role;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.repository.CalendarRepository;
import com.hairbooking.reservation.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;

    @Autowired
    public CalendarService(CalendarRepository calendarRepository, UserRepository userRepository) {
        this.calendarRepository = calendarRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Calendar createCalendarForHairdresser(Long hairdresserId) {
        User hairdresser = userRepository.findById(hairdresserId)
                .orElseThrow(() -> new EntityNotFoundException("Frizer nije pronađen"));

        // ✔️ Provjeravamo da li korisnik ima rolu HAIRDRESSER
        if (!Role.HAIRDRESSER.equals(hairdresser.getRole())) {
            throw new IllegalArgumentException("Samo frizeri mogu imati kalendar!");
        }

        // ✔️ Provjera da li frizer već ima kalendar
        if (calendarRepository.findByHairdresserId(hairdresserId).isPresent()) {
            throw new IllegalStateException("Frizer već ima kalendar!");
        }

        // ✔️ Kreiranje novog kalendara
        Calendar calendar = new Calendar();
        calendar.setHairdresser(hairdresser);
        return calendarRepository.save(calendar);
    }

    @Transactional
    public Calendar getCalendarByHairdresser(Long hairdresserId) {
        User hairdresser = userRepository.findById(hairdresserId)
                .orElseThrow(() -> new EntityNotFoundException("Frizer nije pronađen"));

        return calendarRepository.findByHairdresserId(hairdresserId)
                .orElseThrow(() -> new EntityNotFoundException("Kalendar nije pronađen za ovog frizera"));
    }

    @Transactional
    public Calendar updateCalendar(Long hairdresserId, Calendar updatedCalendar) {
        Calendar existingCalendar = calendarRepository.findByHairdresserId(hairdresserId)
                .orElseThrow(() -> new EntityNotFoundException("Kalendar nije pronađen za ovog frizera"));

        // Postavljanje novih vrijednosti
        existingCalendar.setAppointments(updatedCalendar.getAppointments());

        return calendarRepository.save(existingCalendar);
    }

    @Transactional
    public void deleteCalendar(Long hairdresserId) {
        Calendar calendar = calendarRepository.findByHairdresserId(hairdresserId)
                .orElseThrow(() -> new EntityNotFoundException("Kalendar nije pronađen za ovog frizera"));

        calendarRepository.delete(calendar);
    }
}

