package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.model.Appointment;
import com.hairbooking.reservation.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@CrossOrigin
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/book")
    public ResponseEntity<Appointment> bookAppointment(
            @RequestParam Long calendarId,
            @RequestParam Long serviceId,
            @RequestParam Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime) {

        Appointment appointment = appointmentService.bookAppointment(calendarId, serviceId, customerId, date, startTime);
        return ResponseEntity.ok(appointment);
    }

    // ✅ GET - Dohvatanje svih termina za određeni kalendar (frizerov kalendar)
    @GetMapping("/calendar/{calendarId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByCalendar(@PathVariable Long calendarId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByCalendar(calendarId));
    }

    // ✅ GET - Dohvatanje pojedinačnog termina po ID-u
    @GetMapping("/{appointmentId}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentId));
    }

    // ✅ PUT - Ažuriranje termina
    @PutMapping("/{appointmentId}")
    public ResponseEntity<Appointment> updateAppointment(
            @PathVariable Long appointmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime) {

        Appointment updatedAppointment = appointmentService.updateAppointment(appointmentId, date, startTime);
        return ResponseEntity.ok(updatedAppointment);
    }

    // ✅ DELETE - Brisanje termina
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long appointmentId) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }
}

