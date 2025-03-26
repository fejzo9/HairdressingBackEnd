package com.hairbooking.reservation.controller;

import com.hairbooking.reservation.model.WorkingHours;
import com.hairbooking.reservation.service.WorkingHoursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
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
}
