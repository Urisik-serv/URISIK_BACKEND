package com.urisik.backend.domain.mealplan.dto.req;

import com.urisik.backend.domain.mealplan.entity.MealPlan;

import java.time.LocalDate;
import java.util.List;

public record CreateMealPlanReqDTO(
        LocalDate weekStartDate,
        List<MealPlan.SlotKey> selectedSlots,
        boolean regenerate
) {}
