package com.urisik.backend.domain.mealplan.repository;

import com.urisik.backend.domain.mealplan.entity.MealPlan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    boolean existsByFamilyRoomIdAndWeekStartDate(Long familyRoomId, LocalDate weekStartDate);

    Optional<MealPlan> findByFamilyRoomIdAndWeekStartDate(Long familyRoomId, LocalDate weekStartDate);

    List<MealPlan> findAllByFamilyRoomIdAndWeekStartDateBetweenOrderByWeekStartDateAsc(
            Long familyRoomId, LocalDate start, LocalDate end
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select mp
        from MealPlan mp
        join fetch mp.familyRoom
        where mp.id = :mealPlanId
    """)
    Optional<MealPlan> findByIdForConfirm(@Param("mealPlanId") Long mealPlanId);
}
