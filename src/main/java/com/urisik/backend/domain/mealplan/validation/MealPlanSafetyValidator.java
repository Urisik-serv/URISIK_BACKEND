package com.urisik.backend.domain.mealplan.validation;

public interface MealPlanSafetyValidator {
    void validateFamilySafe(Long familyRoomId, Long recipeId);
}
