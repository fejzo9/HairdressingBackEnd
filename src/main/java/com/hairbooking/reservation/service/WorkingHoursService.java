package com.hairbooking.reservation.service;

import com.hairbooking.reservation.model.Appointment;
import com.hairbooking.reservation.model.Calendar;
import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.model.WorkingHours;
import com.hairbooking.reservation.repository.AppointmentRepository;
import com.hairbooking.reservation.repository.CalendarRepository;
import com.hairbooking.reservation.repository.UserRepository;
import com.hairbooking.reservation.repository.WorkingHoursRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
public class WorkingHoursService {

    private final WorkingHoursRepository workingHoursRepository;
    private final UserRepository userRepository;
    private final CalendarRepository calendarRepository;
    private final AppointmentRepository appointmentRepository;

    public WorkingHoursService(WorkingHoursRepository workingHoursRepository, UserRepository userRepository, CalendarRepository calendarRepository, AppointmentRepository appointmentRepository) {
        this.workingHoursRepository = workingHoursRepository;
        this.userRepository = userRepository;
        this.calendarRepository = calendarRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<WorkingHours> getWorkingHoursByHairdresserId(Long hairdresserId) {
        return workingHoursRepository.findByHairdresserId(hairdresserId);
    }

    public WorkingHours createWorkingHours(Long hairdresserId, WorkingHours workingHours) {
        User hairdresser = userRepository.findById(hairdresserId)
                .orElseThrow(() -> new IllegalArgumentException("Frizer nije pronađen"));
        workingHours.setHairdresser(hairdresser);

        // 🛠️ Ako je neradni dan, ne treba postavljati početak i kraj smjene
        if (workingHours.isDayOff()) {
            workingHours.setStartTime(null);
            workingHours.setEndTime(null);
        }

        return workingHoursRepository.save(workingHours);
    }

    public List<WorkingHours> createListOfWorkingHours(Long hairdresserId, List<WorkingHours> workingHoursList) {
        User hairdresser = userRepository.findById(hairdresserId)
                .orElseThrow(() -> new EntityNotFoundException("Frizer nije pronađen"));

        for (WorkingHours wh : workingHoursList) {
            wh.setHairdresser(hairdresser); // Postavi frizera za svaki dan

            // 🛠️ Ako je neradni dan, ne treba postavljati početak i kraj smjene
            if (wh.isDayOff()) {
                wh.setStartTime(null);
                wh.setEndTime(null);
            }
        }

        return workingHoursRepository.saveAll(workingHoursList);
    }


    public WorkingHours updateWorkingHours(Long id, WorkingHours updated) {
        WorkingHours existing = workingHoursRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Radno vrijeme nije pronađeno"));

        existing.setDayOfWeek(updated.getDayOfWeek());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());

        if(updated.isDayOff()){
            existing.setStartTime(null);
            existing.setEndTime(null);
        }
        return workingHoursRepository.save(existing);
    }

    public void deleteWorkingHours(Long id){
        if(!workingHoursRepository.existsById(id)){
            throw new EntityNotFoundException("Radno vrijeme nije pronađeno za brisanje!");
        }
    }

    public Optional<WorkingHours> getWorkingHoursByHairdresserAndDay(Long hairdresserId, DayOfWeek day) {
        return Optional.ofNullable(workingHoursRepository.findByHairdresserIdAndDayOfWeek(hairdresserId, day)
                .orElseThrow(() -> new EntityNotFoundException("Radno vrijeme za taj dan nije pronađeno")));
    }

    // Dodavanje pauze
    public WorkingHours setBreakForDay(Long hairdresserId, DayOfWeek day, LocalTime breakStart, LocalTime breakEnd) {
        WorkingHours wh = workingHoursRepository.findByHairdresserIdAndDayOfWeek(hairdresserId, day)
                .orElseThrow(() -> new EntityNotFoundException("Radno vrijeme nije pronađeno za taj dan"));

        // 👉 Dohvati kalendar frizera
        Calendar calendar = calendarRepository.findByHairdresserId(hairdresserId)
                .orElseThrow(() -> new EntityNotFoundException("Kalendar nije pronađen za frizera."));

        // 👉 Pretpostavimo da postavljaš pauzu za sljedeći datum tog dana u sedmici
        LocalDate nextDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(day));

        // 👉 Dohvati sve termine tog dana
        List<Appointment> appointments = appointmentRepository.findByCalendarIdAndDate(calendar.getId(), nextDate);

        // 👉 Provjera da li se pauza preklapa s nekim od termina
        boolean overlaps = appointments.stream().anyMatch(appointment ->
                breakStart.isBefore(appointment.getEndTime()) &&
                        breakEnd.isAfter(appointment.getStartTime())
        );

        if (overlaps) {
            throw new IllegalStateException("❌ Pauza se preklapa s postojećim terminima!");
        }

        // 👉 Ako nema preklapanja, postavi pauzu
        wh.setBreakStart(breakStart);
        wh.setBreakEnd(breakEnd);

        return workingHoursRepository.save(wh);
    }


    // Brisanje pauze
    public WorkingHours removeBreakForDay(Long hairdresserId, DayOfWeek day) {
        WorkingHours wh = workingHoursRepository.findByHairdresserIdAndDayOfWeek(hairdresserId, day)
                .orElseThrow(() -> new EntityNotFoundException("Radno vrijeme nije pronađeno za taj dan"));

        wh.setBreakStart(null);
        wh.setBreakEnd(null);

        return workingHoursRepository.save(wh);
    }
}
