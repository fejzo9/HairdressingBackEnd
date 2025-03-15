package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.model.Calendar;
import com.hairbooking.reservation.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Calendar> getCalendar(@PathVariable Long hairdresserId) {
        return ResponseEntity.ok(calendarService.getCalendarByHairdresser(hairdresserId));
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

