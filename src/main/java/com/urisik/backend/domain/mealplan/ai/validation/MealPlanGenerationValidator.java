package com.urisik.backend.domain.mealplan.ai.validation;

import com.urisik.backend.domain.mealplan.entity.MealPlan;
import com.urisik.backend.domain.mealplan.exception.MealPlanException;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanErrorCode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MealPlanGenerationValidator {

    public void validateRecipeAssignments(
            List<MealPlan.SlotKey> selectedSlots,
            Map<MealPlan.SlotKey, Long> recipeAssignments,
            List<Long> candidateRecipeIds
    ) {
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }
        if (selectedSlots.stream().anyMatch(Objects::isNull)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }
        if (candidateRecipeIds == null || candidateRecipeIds.isEmpty() || candidateRecipeIds.stream().anyMatch(Objects::isNull)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }
        if (recipeAssignments == null || recipeAssignments.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        // Reject extra keys that were not requested
        Set<MealPlan.SlotKey> requestedSet = new HashSet<>(selectedSlots);
        if (!recipeAssignments.keySet().equals(requestedSet)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        for (MealPlan.SlotKey slot : selectedSlots) {
            if (!recipeAssignments.containsKey(slot)) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
            Long recipeId = recipeAssignments.get(slot);
            if (recipeId == null) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
        }

        Set<Long> candidateSet = new HashSet<>(candidateRecipeIds);
        for (Long recipeId : recipeAssignments.values()) {
            if (recipeId == null || !candidateSet.contains(recipeId)) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
        }
    }
}
