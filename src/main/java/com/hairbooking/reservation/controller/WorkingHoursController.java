package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.model.WorkingHours;
import com.hairbooking.reservation.service.WorkingHoursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/working-hours")
public class WorkingHoursController {

    private final WorkingHoursService workingHoursService;

    @Autowired
    public WorkingHoursController(WorkingHoursService workingHoursService) {
        this.workingHoursService = workingHoursService;
    }

    @GetMapping("/hairdresser/{hairdresserId}")
    public ResponseEntity<List<WorkingHours>> getWorkingHoursByHairdresser(@PathVariable Long hairdresserId) {
        List<WorkingHours> workingHours = workingHoursService.getWorkingHoursByHairdresserId(hairdresserId);
        return ResponseEntity.ok(workingHours);
    }

    @GetMapping("/hairdresser/{hairdresserId}/day/{day}")
    public ResponseEntity<WorkingHours> getWorkingHoursByDay(
            @PathVariable Long hairdresserId,
            @PathVariable DayOfWeek day) {
        Optional<WorkingHours> workingHours = workingHoursService.getWorkingHoursByHairdresserAndDay(hairdresserId, day);
        return workingHours.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/hairdresser/{hairdresserId}")
    public ResponseEntity<WorkingHours> createWorkingHours(@PathVariable Long hairdresserId, @RequestBody WorkingHours workingHours) {
        WorkingHours saved = workingHoursService.createWorkingHours(hairdresserId, workingHours);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/hairdresser/{hairdresserId}/weekly")
    public ResponseEntity<?> createListOfWorkingHours(
            @PathVariable Long hairdresserId,
            @RequestBody List<WorkingHours> workingHoursList
    ) {
        List<WorkingHours> created = workingHoursService.createListOfWorkingHours(hairdresserId, workingHoursList);
        return ResponseEntity.ok(created);
    }


    @PutMapping("/{id}")
    public ResponseEntity<WorkingHours> updateWorkingHours(
            @PathVariable Long id,
            @RequestBody WorkingHours updated) {
        WorkingHours saved = workingHoursService.updateWorkingHours(id, updated);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkingHours(@PathVariable Long id) {
        workingHoursService.deleteWorkingHours(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/set-break")
    public ResponseEntity<WorkingHours> setBreak(
            @PathVariable Long id,
            @RequestParam  DayOfWeek day,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime breakStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime breakEnd
    ) {
        WorkingHours updated = workingHoursService.setBreakForDay(id, day, breakStart, breakEnd);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/remove-break")
    public ResponseEntity<WorkingHours> removeBreak(@PathVariable Long id, @RequestParam  DayOfWeek day) {
        WorkingHours updated = workingHoursService.removeBreakForDay(id, day);
        return ResponseEntity.ok(updated);
    }
}
