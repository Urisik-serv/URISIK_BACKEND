package com.urisik.backend.domain.mealplan.dto.req;

import com.urisik.backend.domain.mealplan.enums.MealType;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;

public record UpdateMealPlanReqDTO(
        @NotNull SlotRequest slot,
        @NotNull RecipeSelectionDTO selectedRecipe
) {
    public record SlotRequest(
            @NotNull DayOfWeek dayOfWeek,
            @NotNull MealType mealType
    ) {}
}
