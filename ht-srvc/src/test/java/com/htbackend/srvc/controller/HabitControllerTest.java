package com.htbackend.srvc.controller;

import com.htbackend.srvc.dto.HabitRequest;
import com.htbackend.srvc.dto.HabitResponse;
import com.htbackend.srvc.entity.Habit;
import com.htbackend.srvc.entity.HabitFrequency;
import com.htbackend.srvc.service.HabitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitControllerTest {

    @Mock
    private HabitService habitService;

    @InjectMocks
    private HabitController habitController;

    private Habit testHabit;

    @BeforeEach
    void setUp() {
        testHabit = new Habit();
        testHabit.setId(1L);
        testHabit.setName("Morning Exercise");
        testHabit.setDescription("30 minutes cardio");
        testHabit.setFrequency(HabitFrequency.DAILY);
        testHabit.setReminderTime("07:00");
        testHabit.setCreatedAt(LocalDateTime.now());
        testHabit.setCurrentStreak(2);
        testHabit.setLongestStreak(5);
    }

    @Test
    void getAllHabits_ReturnsAllHabits() {
        // Arrange
        Habit habit2 = new Habit();
        habit2.setId(2L);
        habit2.setName("Read Books");
        habit2.setFrequency(HabitFrequency.DAILY);
        habit2.setCreatedAt(LocalDateTime.now());

        when(habitService.getAllHabits()).thenReturn(Arrays.asList(testHabit, habit2));

        // Act
        ResponseEntity<List<HabitResponse>> result = habitController.getAllHabits();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
        assertEquals("Morning Exercise", result.getBody().get(0).getName());
    }

    @Test
    void getHabitById_WhenExists_ReturnsHabit() {
        // Arrange
        when(habitService.getHabitById(1L)).thenReturn(Optional.of(testHabit));

        // Act
        ResponseEntity<HabitResponse> result = habitController.getHabitById(1L);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1L, result.getBody().getId());
        assertEquals("Morning Exercise", result.getBody().getName());
    }

    @Test
    void getHabitById_WhenNotExists_Returns404() {
        // Arrange
        when(habitService.getHabitById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<HabitResponse> result = habitController.getHabitById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void createHabit_WithValidData_Returns201() {
        // Arrange
        HabitRequest request = new HabitRequest();
        request.setName("New Habit");
        request.setDescription("New description");
        request.setFrequency(HabitFrequency.WEEKLY);
        request.setReminderTime("10:00");

        when(habitService.createHabit(any(Habit.class))).thenAnswer(invocation -> {
            Habit habit = invocation.getArgument(0);
            habit.setId(3L);
            return habit;
        });

        // Act
        ResponseEntity<HabitResponse> result = habitController.createHabit(request);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(3L, result.getBody().getId());
        assertEquals("New Habit", result.getBody().getName());
    }

    @Test
    void updateHabit_WithValidData_Returns200() {
        // Arrange
        HabitRequest request = new HabitRequest();
        request.setName("Updated Exercise");
        request.setDescription("Updated description");
        request.setFrequency(HabitFrequency.WEEKLY);
        request.setReminderTime("08:00");

        when(habitService.updateHabit(eq(1L), any(Habit.class))).thenReturn(Optional.of(testHabit));

        // Act
        ResponseEntity<HabitResponse> result = habitController.updateHabit(1L, request);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void updateHabit_WhenNotExists_Returns404() {
        // Arrange
        HabitRequest request = new HabitRequest();
        request.setName("Updated");

        when(habitService.updateHabit(eq(999L), any(Habit.class))).thenReturn(Optional.empty());

        // Act
        ResponseEntity<HabitResponse> result = habitController.updateHabit(999L, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void deleteHabit_WhenExists_Returns204() {
        // Arrange
        when(habitService.deleteHabit(1L)).thenReturn(true);

        // Act
        ResponseEntity<Void> result = habitController.deleteHabit(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    void deleteHabit_WhenNotExists_Returns404() {
        // Arrange
        when(habitService.deleteHabit(999L)).thenReturn(false);

        // Act
        ResponseEntity<Void> result = habitController.deleteHabit(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void markComplete_WithValidData_ReturnsSuccess() {
        // Arrange
        when(habitService.markHabitComplete(eq(1L), any())).thenReturn(new com.htbackend.srvc.entity.HabitCompletion());

        // Act
        ResponseEntity<Map<String, Object>> result = habitController.markComplete(1L, Map.of("date", "2026-04-26"));

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(true, result.getBody().get("success"));
        assertEquals(1L, result.getBody().get("habitId"));
    }

    @Test
    void markComplete_WithTodayDate_ReturnsSuccess() {
        // Arrange
        when(habitService.markHabitComplete(eq(1L), any())).thenReturn(new com.htbackend.srvc.entity.HabitCompletion());

        // Act
        ResponseEntity<Map<String, Object>> result = habitController.markComplete(1L, Map.of());

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(true, result.getBody().get("success"));
    }

    @Test
    void markComplete_WhenHabitNotFound_Returns404() {
        // Arrange
        when(habitService.markHabitComplete(eq(999L), any()))
                .thenThrow(new IllegalArgumentException("Habit not found"));

        // Act
        ResponseEntity<Map<String, Object>> result = habitController.markComplete(999L, Map.of());

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void unmarkComplete_WhenExists_ReturnsSuccess() {
        // Arrange
        when(habitService.unmarkHabitComplete(eq(1L), any())).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Object>> result = habitController.unmarkComplete(1L, Map.of("date", "2026-04-26"));

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(true, result.getBody().get("success"));
    }

    @Test
    void unmarkComplete_WhenNotExists_Returns200WithFalse() {
        // Arrange
        when(habitService.unmarkHabitComplete(eq(1L), any())).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Object>> result = habitController.unmarkComplete(1L, Map.of("date", "2026-04-26"));

        // Assert
        // Note: Controller returns 200 with success=false, not 404
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(false, result.getBody().get("success"));
    }
}