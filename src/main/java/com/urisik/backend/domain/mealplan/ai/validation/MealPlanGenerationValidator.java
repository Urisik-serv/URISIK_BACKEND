package com.urisik.backend.domain.mealplan.ai.validation;

import com.urisik.backend.domain.mealplan.entity.MealPlan;
import com.urisik.backend.domain.mealplan.exception.MealPlanException;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
        if (recipeAssignments == null) {
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
            if (!candidateRecipeIds.contains(recipeId)) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
        }
    }
}
