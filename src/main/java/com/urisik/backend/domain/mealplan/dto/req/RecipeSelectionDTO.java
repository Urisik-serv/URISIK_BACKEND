package com.urisik.backend.domain.mealplan.dto.req;

import jakarta.validation.constraints.NotNull;

public record RecipeSelectionDTO(
        @NotNull RecipeSelectionType type,
        @NotNull Long id
) {
    public enum RecipeSelectionType {
        RECIPE,
        TRANSFORMED_RECIPE
    }
}
