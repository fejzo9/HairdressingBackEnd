package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.dto.CancelRequest;
import com.hairbooking.reservation.dto.RescheduleRequest;
import com.hairbooking.reservation.model.Appointment;
import com.hairbooking.reservation.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // ✅ DELETE - Brisanje termina (only owner, hairdresser, or admin)
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long appointmentId, Authentication authentication) {
        appointmentService.deleteAppointment(appointmentId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // ✅ POST - Cancel appointment (soft cancel, sets status to CANCELLED)
    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestBody(required = false) CancelRequest cancelRequest,
            Authentication authentication) {

        String reason = cancelRequest != null ? cancelRequest.getReason() : null;
        Appointment appointment = appointmentService.cancelAppointment(appointmentId, reason, authentication.getName());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Appointment cancelled successfully");
        response.put("appointment", appointment);
        return ResponseEntity.ok(response);
    }

    // ✅ PATCH - Reschedule appointment
    @PatchMapping("/{appointmentId}/reschedule")
    public ResponseEntity<Appointment> rescheduleAppointment(
            @PathVariable Long appointmentId,
            @RequestBody RescheduleRequest rescheduleRequest,
            Authentication authentication) {

        Appointment appointment = appointmentService.rescheduleAppointment(
                appointmentId,
                rescheduleRequest.getNewDate(),
                rescheduleRequest.getNewStartTime(),
                authentication.getName());

        return ResponseEntity.ok(appointment);
    }
}

