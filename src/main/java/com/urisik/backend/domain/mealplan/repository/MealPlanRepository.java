package com.urisik.backend.domain.mealplan.repository;

import com.urisik.backend.domain.mealplan.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    boolean existsByFamilyRoomIdAndWeekStartDate(Long familyRoomId, LocalDate weekStartDate);
    Optional<MealPlan> findByFamilyRoomIdAndWeekStartDate(Long familyRoomId, LocalDate weekStartDate);
}
