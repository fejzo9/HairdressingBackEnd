package com.hairbooking.reservation.repository;

import com.hairbooking.reservation.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByCalendarId(Long calendarId);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.calendar.id = :calendarId AND a.date = :date AND ((a.startTime <= :endTime AND a.endTime > :startTime))")
    boolean existsOverlappingAppointment(@Param("calendarId") Long calendarId, @Param("date") LocalDate date, @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

}

