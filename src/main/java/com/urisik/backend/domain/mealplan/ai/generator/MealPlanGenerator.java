package com.urisik.backend.domain.mealplan.ai.generator;

import com.urisik.backend.domain.mealplan.dto.req.RecipeSelectionDTO;
import com.urisik.backend.domain.mealplan.entity.MealPlan;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface MealPlanGenerator {

    Map<MealPlan.SlotKey, RecipeSelectionDTO> generateRecipeAssignments(
            List<MealPlan.SlotKey> selectedSlots,
            List<RecipeSelectionDTO> candidateSelections
    );

    default Map<MealPlan.SlotKey, RecipeSelectionDTO> generateRecipeAssignmentsFromRecipeIds(
            List<MealPlan.SlotKey> selectedSlots,
            List<Long> recipeIds
    ) {
        List<RecipeSelectionDTO> candidates = recipeIds == null ? List.of()
                : recipeIds.stream()
                .filter(Objects::nonNull)
                .map(id -> new RecipeSelectionDTO(
                        RecipeSelectionDTO.RecipeSelectionType.RECIPE,
                        id,
                        id
                ))
                .toList();

        return generateRecipeAssignments(selectedSlots, candidates);
    }
}
