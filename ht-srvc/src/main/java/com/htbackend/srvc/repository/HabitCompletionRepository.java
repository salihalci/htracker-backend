package com.htbackend.srvc.repository;

import com.htbackend.srvc.entity.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    List<HabitCompletion> findByHabitId(Long habitId);

    Optional<HabitCompletion> findByHabitIdAndCompletedDate(Long habitId, LocalDate completedDate);

    @Query("SELECT hc FROM HabitCompletion hc WHERE hc.habit.id = :habitId AND hc.completedDate BETWEEN :startDate AND :endDate ORDER BY hc.completedDate DESC")
    List<HabitCompletion> findByHabitIdAndDateRange(Long habitId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(hc) FROM HabitCompletion hc WHERE hc.habit.id = :habitId")
    Long countByHabitId(Long habitId);

    @Query("SELECT hc FROM HabitCompletion hc WHERE hc.completedDate = :date")
    List<HabitCompletion> findByCompletedDate(LocalDate date);

    List<HabitCompletion> findByHabitIdOrderByCompletedDateDesc(Long habitId);
}