package com.htbackend.srvc.service;

import com.htbackend.srvc.entity.Habit;
import com.htbackend.srvc.entity.HabitCompletion;
import com.htbackend.srvc.entity.HabitFrequency;
import com.htbackend.srvc.repository.HabitCompletionRepository;
import com.htbackend.srvc.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private HabitCompletionRepository completionRepository;

    @InjectMocks
    private HabitService habitService;

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
    }

    @Test
    void getAllHabits_ReturnsAllHabits() {
        // Arrange
        Habit habit2 = new Habit();
        habit2.setId(2L);
        habit2.setName("Read Books");
        habit2.setFrequency(HabitFrequency.DAILY);
        habit2.setCreatedAt(LocalDateTime.now());

        when(habitRepository.findByOrderByCreatedAtDesc()).thenReturn(Arrays.asList(testHabit, habit2));
        when(completionRepository.findByHabitIdOrderByCompletedDateDesc(any())).thenReturn(List.of());

        // Act
        List<Habit> habits = habitService.getAllHabits();

        // Assert
        assertEquals(2, habits.size());
        verify(habitRepository, times(1)).findByOrderByCreatedAtDesc();
    }

    @Test
    void getHabitById_WhenExists_ReturnsHabit() {
        // Arrange
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));
        when(completionRepository.findByHabitIdOrderByCompletedDateDesc(1L)).thenReturn(List.of());

        // Act
        Optional<Habit> result = habitService.getHabitById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Morning Exercise", result.get().getName());
    }

    @Test
    void getHabitById_WhenNotExists_ReturnsEmpty() {
        // Arrange
        when(habitRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Habit> result = habitService.getHabitById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void createHabit_SavesAndReturnsHabit() {
        // Arrange
        when(habitRepository.save(any(Habit.class))).thenAnswer(invocation -> {
            Habit habit = invocation.getArgument(0);
            habit.setId(1L);
            return habit;
        });

        // Act
        Habit result = habitService.createHabit(testHabit);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(habitRepository, times(1)).save(any(Habit.class));
    }

    @Test
    void updateHabit_WhenExists_UpdatesAndReturns() {
        // Arrange
        Habit updatedDetails = new Habit();
        updatedDetails.setName("Updated Exercise");
        updatedDetails.setDescription("Updated description");
        updatedDetails.setFrequency(HabitFrequency.WEEKLY);
        updatedDetails.setReminderTime("08:00");

        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));
        when(habitRepository.save(any(Habit.class))).thenReturn(testHabit);

        // Act
        Optional<Habit> result = habitService.updateHabit(1L, updatedDetails);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated Exercise", result.get().getName());
        assertEquals(HabitFrequency.WEEKLY, result.get().getFrequency());
    }

    @Test
    void updateHabit_WhenNotExists_ReturnsEmpty() {
        // Arrange
        when(habitRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Habit> result = habitService.updateHabit(999L, testHabit);

        // Assert
        assertFalse(result.isPresent());
        verify(habitRepository, never()).save(any());
    }

    @Test
    void deleteHabit_WhenExists_ReturnsTrue() {
        // Arrange
        when(habitRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = habitService.deleteHabit(1L);

        // Assert
        assertTrue(result);
        verify(habitRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteHabit_WhenNotExists_ReturnsFalse() {
        // Arrange
        when(habitRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = habitService.deleteHabit(999L);

        // Assert
        assertFalse(result);
        verify(habitRepository, never()).deleteById(any());
    }

    @Test
    void markHabitComplete_WhenNotCompleted_CreatesCompletion() {
        // Arrange
        LocalDate today = LocalDate.now();
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));
        when(completionRepository.findByHabitIdAndCompletedDate(1L, today)).thenReturn(Optional.empty());
        when(completionRepository.save(any(HabitCompletion.class))).thenAnswer(invocation -> {
            HabitCompletion completion = invocation.getArgument(0);
            completion.setId(1L);
            return completion;
        });

        // Act
        HabitCompletion result = habitService.markHabitComplete(1L, today);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(completionRepository, times(1)).save(any(HabitCompletion.class));
    }

    @Test
    void markHabitComplete_WhenAlreadyCompleted_ReturnsExisting() {
        // Arrange
        LocalDate today = LocalDate.now();
        HabitCompletion existingCompletion = new HabitCompletion(testHabit, today);
        existingCompletion.setId(1L);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));
        when(completionRepository.findByHabitIdAndCompletedDate(1L, today)).thenReturn(Optional.of(existingCompletion));

        // Act
        HabitCompletion result = habitService.markHabitComplete(1L, today);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(completionRepository, never()).save(any());
    }

    @Test
    void markHabitComplete_WhenHabitNotFound_ThrowsException() {
        // Arrange
        when(habitRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> habitService.markHabitComplete(999L, LocalDate.now()));
    }

    @Test
    void unmarkHabitComplete_WhenExists_DeletesAndReturnsTrue() {
        // Arrange
        LocalDate today = LocalDate.now();
        HabitCompletion completion = new HabitCompletion(testHabit, today);
        
        when(completionRepository.findByHabitIdAndCompletedDate(1L, today)).thenReturn(Optional.of(completion));

        // Act
        boolean result = habitService.unmarkHabitComplete(1L, today);

        // Assert
        assertTrue(result);
        verify(completionRepository, times(1)).delete(completion);
    }

    @Test
    void unmarkHabitComplete_WhenNotExists_ReturnsFalse() {
        // Arrange
        LocalDate today = LocalDate.now();
        when(completionRepository.findByHabitIdAndCompletedDate(1L, today)).thenReturn(Optional.empty());

        // Act
        boolean result = habitService.unmarkHabitComplete(1L, today);

        // Assert
        assertFalse(result);
        verify(completionRepository, never()).delete(any());
    }

    @Test
    void getCompletionsForHabit_ReturnsCompletions() {
        // Arrange
        HabitCompletion completion1 = new HabitCompletion(testHabit, LocalDate.now().minusDays(1));
        HabitCompletion completion2 = new HabitCompletion(testHabit, LocalDate.now());
        
        when(completionRepository.findByHabitId(1L)).thenReturn(Arrays.asList(completion1, completion2));

        // Act
        List<HabitCompletion> result = habitService.getCompletionsForHabit(1L);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void getTodayCompletions_ReturnsTodayCompletions() {
        // Arrange
        HabitCompletion completion = new HabitCompletion(testHabit, LocalDate.now());
        when(completionRepository.findByCompletedDate(LocalDate.now())).thenReturn(List.of(completion));

        // Act
        List<HabitCompletion> result = habitService.getTodayCompletions();

        // Assert
        assertEquals(1, result.size());
    }
}