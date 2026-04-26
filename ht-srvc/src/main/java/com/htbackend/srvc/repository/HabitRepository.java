package com.htbackend.srvc.repository;

import com.htbackend.srvc.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {

    List<Habit> findByOrderByCreatedAtDesc();

    Optional<Habit> findByName(String name);

    @Query("SELECT h FROM Habit h WHERE h.createdAt >= :startDate ORDER BY h.createdAt DESC")
    List<Habit> findRecentHabits(LocalDate startDate);
}