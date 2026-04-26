package com.htbackend.srvc.dto;

import com.htbackend.srvc.entity.Habit;
import com.htbackend.srvc.entity.HabitFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "Response payload for habit data")
public class HabitResponse {

    @Schema(description = "Unique identifier of the habit")
    private Long id;

    @Schema(description = "Name of the habit")
    private String name;

    @Schema(description = "Description of the habit")
    private String description;

    @Schema(description = "Frequency of the habit")
    private HabitFrequency frequency;

    @Schema(description = "Reminder time in HH:mm format")
    private String reminderTime;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Current streak of consecutive completions")
    private Integer currentStreak;

    @Schema(description = "Longest streak ever achieved")
    private Integer longestStreak;

    @Schema(description = "List of dates when habit was completed")
    private List<LocalDateTime> completionDates;

    public static HabitResponse fromEntity(Habit habit) {
        HabitResponse response = new HabitResponse();
        response.setId(habit.getId());
        response.setName(habit.getName());
        response.setDescription(habit.getDescription());
        response.setFrequency(habit.getFrequency());
        response.setReminderTime(habit.getReminderTime());
        response.setCreatedAt(habit.getCreatedAt());
        response.setUpdatedAt(habit.getUpdatedAt());
        response.setCurrentStreak(habit.getCurrentStreak());
        response.setLongestStreak(habit.getLongestStreak());
        response.setCompletionDates(habit.getCompletions().stream()
                .map(c -> c.getCompletedDate().atStartOfDay())
                .collect(Collectors.toList()));
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public HabitFrequency getFrequency() { return frequency; }
    public void setFrequency(HabitFrequency frequency) { this.frequency = frequency; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }

    public Integer getLongestStreak() { return longestStreak; }
    public void setLongestStreak(Integer longestStreak) { this.longestStreak = longestStreak; }

    public List<LocalDateTime> getCompletionDates() { return completionDates; }
    public void setCompletionDates(List<LocalDateTime> completionDates) { this.completionDates = completionDates; }
}