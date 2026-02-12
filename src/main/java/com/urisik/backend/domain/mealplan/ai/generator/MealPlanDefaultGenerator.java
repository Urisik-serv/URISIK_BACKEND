package com.urisik.backend.domain.mealplan.ai.generator;

import com.urisik.backend.domain.mealplan.dto.common.RecipeSelectionDTO;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
import com.urisik.backend.domain.mealplan.exception.MealPlanException;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanErrorCode;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class MealPlanDefaultGenerator implements MealPlanGenerator {

    @Override
    public Map<MealPlan.SlotKey, RecipeSelectionDTO> generateRecipeAssignments(
            List<MealPlan.SlotKey> selectedSlots,
            List<RecipeSelectionDTO> candidateSelections
    ) {
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED);
        }
        if (selectedSlots.stream().anyMatch(Objects::isNull)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED);
        }
        if (candidateSelections == null || candidateSelections.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED);
        }
        if (candidateSelections.stream().anyMatch(Objects::isNull)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED);
        }

        Map<MealPlan.SlotKey, RecipeSelectionDTO> result = new HashMap<>();
        int i = 0;
        for (MealPlan.SlotKey slot : selectedSlots) {
            RecipeSelectionDTO chosen = candidateSelections.get(i % candidateSelections.size());
            result.put(slot, chosen);
            i++;
        }
        return result;
    }
}
