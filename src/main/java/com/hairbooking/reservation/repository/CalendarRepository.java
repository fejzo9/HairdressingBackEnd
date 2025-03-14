package com.hairbooking.reservation.repository;

import com.hairbooking.reservation.model.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    Optional<Calendar> findByHairdresserId(Long hairdresserId);
    boolean existsByHairdresserId(Long hairdresserId);
}
