package com.urisik.backend.domain.mealplan.dto.common;

import jakarta.validation.constraints.NotNull;

public record RecipeSelectionDTO(
        @NotNull RecipeSelectionType type,
        @NotNull Long id,
        @NotNull Long baseRecipeId
) {
    public enum RecipeSelectionType {
        RECIPE,
        TRANSFORMED_RECIPE
    }
}
