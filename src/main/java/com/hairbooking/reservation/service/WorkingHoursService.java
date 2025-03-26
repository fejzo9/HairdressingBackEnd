package com.hairbooking.reservation.service;

import com.hairbooking.reservation.model.User;
import com.hairbooking.reservation.model.WorkingHours;
import com.hairbooking.reservation.repository.UserRepository;
import com.hairbooking.reservation.repository.WorkingHoursRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Service
public class WorkingHoursService {

    private final WorkingHoursRepository workingHoursRepository;
    private final UserRepository userRepository;

    public WorkingHoursService(WorkingHoursRepository workingHoursRepository, UserRepository userRepository) {
        this.workingHoursRepository = workingHoursRepository;
        this.userRepository = userRepository;
    }

    public List<WorkingHours> getWorkingHoursByHairdresserId(Long hairdresserId) {
        return workingHoursRepository.findByHairdresserId(hairdresserId);
    }

    public WorkingHours createWorkingHours(Long hairdresserId, WorkingHours workingHours) {
        User hairdresser = userRepository.findById(hairdresserId)
                .orElseThrow(() -> new IllegalArgumentException("Frizer nije pronađen"));
        workingHours.setHairdresser(hairdresser);
        return workingHoursRepository.save(workingHours);
    }

    public WorkingHours updateWorkingHours(Long id, WorkingHours updated) {
        WorkingHours existing = workingHoursRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Radno vrijeme nije pronađeno"));

        existing.setDayOfWeek(updated.getDayOfWeek());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
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

}
