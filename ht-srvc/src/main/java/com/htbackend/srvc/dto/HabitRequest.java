package com.htbackend.srvc.dto;

import com.htbackend.srvc.entity.HabitFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for creating or updating a habit")
public class HabitRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "Name of the habit", example = "Morning Exercise")
    private String name;

    @Schema(description = "Description of the habit", example = "30 minutes of cardio")
    private String description;

    @Schema(description = "Frequency of the habit", example = "DAILY", allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
    private HabitFrequency frequency;

    @Schema(description = "Reminder time in HH:mm format", example = "07:00")
    private String reminderTime;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public HabitFrequency getFrequency() { return frequency; }
    public void setFrequency(HabitFrequency frequency) { this.frequency = frequency; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
}