package com.urisik.backend.domain.mealplan.ai.generator;

import com.urisik.backend.domain.mealplan.entity.MealPlan;

import java.util.List;
import java.util.Map;

public interface MealPlanGenerator {
    Map<MealPlan.SlotKey, Long> generateRecipeAssignments(
            List<MealPlan.SlotKey> selectedSlots,
            List<Long> candidateRecipeIds
    );
}
