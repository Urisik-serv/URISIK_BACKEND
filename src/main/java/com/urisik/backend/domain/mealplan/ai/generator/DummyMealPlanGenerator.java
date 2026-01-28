package com.urisik.backend.domain.mealplan.ai.generator;

import com.urisik.backend.domain.mealplan.entity.MealPlan;
import com.urisik.backend.domain.mealplan.exception.MealPlanException;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanErrorCode;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Profile({"default","local","dev"})
@Component
public class DummyMealPlanGenerator implements MealPlanGenerator {

    @Override
    public Map<MealPlan.SlotKey, Long> generateRecipeAssignments(
            List<MealPlan.SlotKey> selectedSlots,
            List<Long> candidateRecipeIds
    ) {
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED);
        }
        if (candidateRecipeIds == null || candidateRecipeIds.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED);
        }

        Map<MealPlan.SlotKey, Long> result = new HashMap<>();
        int i = 0;
        for (MealPlan.SlotKey slot : selectedSlots) {
            result.put(slot, candidateRecipeIds.get(i % candidateRecipeIds.size()));
            i++;
        }
        return result;
    }
}
