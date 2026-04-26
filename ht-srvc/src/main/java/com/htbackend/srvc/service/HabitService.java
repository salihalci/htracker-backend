package com.htbackend.srvc.service;

import com.htbackend.srvc.entity.Habit;
import com.htbackend.srvc.entity.HabitCompletion;
import com.htbackend.srvc.entity.HabitFrequency;
import com.htbackend.srvc.repository.HabitCompletionRepository;
import com.htbackend.srvc.repository.HabitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitCompletionRepository completionRepository;

    public HabitService(HabitRepository habitRepository, HabitCompletionRepository completionRepository) {
        this.habitRepository = habitRepository;
        this.completionRepository = completionRepository;
    }

    // Habit CRUD operations
    public List<Habit> getAllHabits() {
        List<Habit> habits = habitRepository.findByOrderByCreatedAtDesc();
        habits.forEach(this::calculateStreaks);
        return habits;
    }

    public Optional<Habit> getHabitById(Long id) {
        return habitRepository.findById(id).map(habit -> {
            calculateStreaks(habit);
            return habit;
        });
    }

    public Habit createHabit(Habit habit) {
        habit.setCreatedAt(LocalDate.now().atStartOfDay());
        return habitRepository.save(habit);
    }

    public Optional<Habit> updateHabit(Long id, Habit updatedHabit) {
        return habitRepository.findById(id).map(habit -> {
            habit.setName(updatedHabit.getName());
            habit.setDescription(updatedHabit.getDescription());
            habit.setFrequency(updatedHabit.getFrequency());
            habit.setReminderTime(updatedHabit.getReminderTime());
            return habitRepository.save(habit);
        });
    }

    public boolean deleteHabit(Long id) {
        if (habitRepository.existsById(id)) {
            habitRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Habit Completion operations
    public HabitCompletion markHabitComplete(Long habitId, LocalDate date) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found with id: " + habitId));

        Optional<HabitCompletion> existing = completionRepository.findByHabitIdAndCompletedDate(habitId, date);
        if (existing.isPresent()) {
            return existing.get();
        }

        HabitCompletion completion = new HabitCompletion(habit, date);
        return completionRepository.save(completion);
    }

    public boolean unmarkHabitComplete(Long habitId, LocalDate date) {
        return completionRepository.findByHabitIdAndCompletedDate(habitId, date)
                .map(completion -> {
                    completionRepository.delete(completion);
                    return true;
                })
                .orElse(false);
    }

    public List<HabitCompletion> getCompletionsForHabit(Long habitId) {
        return completionRepository.findByHabitId(habitId);
    }

    public List<HabitCompletion> getCompletionsForHabitInRange(Long habitId, LocalDate startDate, LocalDate endDate) {
        return completionRepository.findByHabitIdAndDateRange(habitId, startDate, endDate);
    }

    public List<HabitCompletion> getTodayCompletions() {
        return completionRepository.findByCompletedDate(LocalDate.now());
    }

    // Streak calculation
    private void calculateStreaks(Habit habit) {
        List<HabitCompletion> completions = completionRepository.findByHabitIdOrderByCompletedDateDesc(habit.getId());
        
        if (completions.isEmpty()) {
            habit.setCurrentStreak(0);
            habit.setLongestStreak(0);
            return;
        }

        int currentStreak = 0;
        int longestStreak = 0;
        int tempStreak = 0;
        LocalDate lastDate = null;

        for (HabitCompletion completion : completions) {
            LocalDate date = completion.getCompletedDate();
            
            if (lastDate == null) {
                tempStreak = 1;
                lastDate = date;
            } else {
                long daysBetween = ChronoUnit.DAYS.between(lastDate, date);
                if (daysBetween == 1) {
                    tempStreak++;
                } else {
                    longestStreak = Math.max(longestStreak, tempStreak);
                    tempStreak = 1;
                }
                lastDate = date;
            }
        }
        
        longestStreak = Math.max(longestStreak, tempStreak);
        
        // Check if current streak is active (completed today or yesterday)
        LocalDate today = LocalDate.now();
        LocalDate mostRecentCompletion = completions.get(0).getCompletedDate();
        long daysSinceLastCompletion = ChronoUnit.DAYS.between(mostRecentCompletion, today);
        
        if (daysSinceLastCompletion <= 1) {
            // Calculate current streak from most recent backwards
            currentStreak = 1;
            LocalDate checkDate = mostRecentCompletion;
            for (int i = 1; i < completions.size(); i++) {
                LocalDate prevDate = completions.get(i).getCompletedDate();
                if (ChronoUnit.DAYS.between(prevDate, checkDate) == 1) {
                    currentStreak++;
                    checkDate = prevDate;
                } else {
                    break;
                }
            }
        }

        habit.setCurrentStreak(currentStreak);
        habit.setLongestStreak(longestStreak);
    }
}