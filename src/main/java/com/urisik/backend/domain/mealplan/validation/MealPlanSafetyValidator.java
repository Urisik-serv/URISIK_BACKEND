package com.urisik.backend.domain.mealplan.validation;

import com.urisik.backend.domain.mealplan.dto.common.RecipeSelectionDTO;

public interface MealPlanSafetyValidator {

    void validateFamilySafe(Long familyRoomId, RecipeSelectionDTO selection);

    default void validateFamilySafe(Long familyRoomId, Long recipeId) {
        if (recipeId == null) return;
        validateFamilySafe(
                familyRoomId,
                new RecipeSelectionDTO(RecipeSelectionDTO.RecipeSelectionType.RECIPE, recipeId, recipeId)
        );
    }
}
