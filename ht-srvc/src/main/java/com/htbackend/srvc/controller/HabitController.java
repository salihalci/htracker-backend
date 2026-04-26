package com.htbackend.srvc.controller;

import com.htbackend.srvc.dto.HabitRequest;
import com.htbackend.srvc.dto.HabitResponse;
import com.htbackend.srvc.entity.Habit;
import com.htbackend.srvc.entity.HabitCompletion;
import com.htbackend.srvc.service.HabitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/habits")
@Tag(name = "Habits", description = "Habit management endpoints")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    // Get all habits
    @GetMapping
    @Operation(summary = "Get all habits", description = "Retrieve a list of all habits with their completion status")
    public ResponseEntity<List<HabitResponse>> getAllHabits() {
        List<HabitResponse> habits = habitService.getAllHabits().stream()
                .map(HabitResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(habits);
    }

    // Get habit by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get habit by ID", description = "Retrieve a specific habit by its ID")
    public ResponseEntity<HabitResponse> getHabitById(@Parameter(description = "Habit ID") @PathVariable Long id) {
        return habitService.getHabitById(id)
                .map(habit -> ResponseEntity.ok(HabitResponse.fromEntity(habit)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Create new habit
    @PostMapping
    @Operation(summary = "Create a new habit", description = "Create a new habit with the provided details")
    public ResponseEntity<HabitResponse> createHabit(@Valid @RequestBody HabitRequest request) {
        Habit habit = new Habit();
        habit.setName(request.getName());
        habit.setDescription(request.getDescription());
        habit.setFrequency(request.getFrequency() != null ? request.getFrequency() : com.htbackend.srvc.entity.HabitFrequency.DAILY);
        habit.setReminderTime(request.getReminderTime());

        Habit saved = habitService.createHabit(habit);
        return ResponseEntity.status(HttpStatus.CREATED).body(HabitResponse.fromEntity(saved));
    }

    // Update habit
    @PutMapping("/{id}")
    @Operation(summary = "Update a habit", description = "Update an existing habit by its ID")
    public ResponseEntity<HabitResponse> updateHabit(@Parameter(description = "Habit ID") @PathVariable Long id, @Valid @RequestBody HabitRequest request) {
        Habit habit = new Habit();
        habit.setName(request.getName());
        habit.setDescription(request.getDescription());
        habit.setFrequency(request.getFrequency());
        habit.setReminderTime(request.getReminderTime());

        return habitService.updateHabit(id, habit)
                .map(updated -> ResponseEntity.ok(HabitResponse.fromEntity(updated)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete habit
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a habit", description = "Delete a habit by its ID")
    public ResponseEntity<Void> deleteHabit(@Parameter(description = "Habit ID") @PathVariable Long id) {
        if (habitService.deleteHabit(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Mark habit as complete for a specific date
    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark habit as complete", description = "Mark a habit as completed for a specific date")
    public ResponseEntity<Map<String, Object>> markComplete(@Parameter(description = "Habit ID") @PathVariable Long id, @RequestBody Map<String, String> body) {
        LocalDate date = body.containsKey("date") 
                ? LocalDate.parse(body.get("date")) 
                : LocalDate.now();
        
        try {
            HabitCompletion completion = habitService.markHabitComplete(id, date);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "habitId", id,
                "date", date.toString(),
                "message", "Habit marked as complete"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Unmark habit completion
    @DeleteMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> unmarkComplete(@PathVariable Long id, @RequestBody Map<String, String> body) {
        LocalDate date = body.containsKey("date") 
                ? LocalDate.parse(body.get("date")) 
                : LocalDate.now();
        
        boolean removed = habitService.unmarkHabitComplete(id, date);
        if (removed) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "habitId", id,
                "date", date.toString(),
                "message", "Habit unmarked"
            ));
        }
        return ResponseEntity.ok(Map.of(
            "success", false,
            "message", "No completion found for this date"
        ));
    }

    // Get completions for a habit
    @GetMapping("/{id}/completions")
    public ResponseEntity<List<HabitCompletion>> getCompletions(@PathVariable Long id) {
        return ResponseEntity.ok(habitService.getCompletionsForHabit(id));
    }

    // Get today's completions
    @GetMapping("/completions/today")
    public ResponseEntity<List<HabitCompletion>> getTodayCompletions() {
        return ResponseEntity.ok(habitService.getTodayCompletions());
    }
}