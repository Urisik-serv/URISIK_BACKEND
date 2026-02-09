package com.urisik.backend.domain.mealplan.dto.common;

public record RecipeDTO(
        RecipeType type,
        Long id,
        String title
) {
    public enum RecipeType {
        RECIPE,
        TRANSFORMED_RECIPE
    }
}
