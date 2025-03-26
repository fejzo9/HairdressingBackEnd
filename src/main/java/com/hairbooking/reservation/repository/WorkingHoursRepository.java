package com.hairbooking.reservation.repository;

import com.hairbooking.reservation.model.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {
    List<WorkingHours> findByHairdresserId(Long hairdresserId);

    Optional<WorkingHours> findByHairdresserIdAndDayOfWeek(Long hairdresserId, DayOfWeek dayOfWeek);

    boolean existsById(Long id);
}
