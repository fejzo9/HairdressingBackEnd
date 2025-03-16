package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.model.Calendar;
import com.hairbooking.reservation.service.CalendarService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/calendars")
@CrossOrigin
public class CalendarController {

    private final CalendarService calendarService;

    @Autowired
    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @PostMapping("/hairdresser/{hairdresserId}")
    public ResponseEntity<Calendar> createCalendar(@PathVariable Long hairdresserId) {
        return ResponseEntity.ok(calendarService.createCalendarForHairdresser(hairdresserId));
    }

    @GetMapping("/hairdresser/{hairdresserId}")
    public ResponseEntity<?> getCalendar(@PathVariable Long hairdresserId) {
        System.out.println("🔎 Pozvan endpoint za dohvaćanje kalendara frizera ID: " + hairdresserId);

        try {
            Calendar calendar = calendarService.getCalendarByHairdresser(hairdresserId);
            System.out.println("✅ Uspješno dohvaćen kalendar: " + calendar.getId());
            return ResponseEntity.ok(calendar);
        } catch (EntityNotFoundException e) {
            System.out.println("❌ Greška: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found umjesto 403
        } catch (Exception e) {
            System.out.println("❌ Neočekivana greška: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Došlo je do greške.");
        }
    }


    @PutMapping("/hairdresser/{hairdresserId}")
    public ResponseEntity<Calendar> updateCalendar(@PathVariable Long hairdresserId, @RequestBody Calendar updatedCalendar) {
        return ResponseEntity.ok(calendarService.updateCalendar(hairdresserId, updatedCalendar));
    }

    @DeleteMapping("/hairdresser/{hairdresserId}")
    public ResponseEntity<Void> deleteCalendar(@PathVariable Long hairdresserId) {
        calendarService.deleteCalendar(hairdresserId);
        return ResponseEntity.noContent().build();
    }
}

