package com.urisik.backend.domain.mealplan.dto.req;

import com.urisik.backend.domain.mealplan.enums.MealType;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.util.List;

public record UpdateMealPlanReqDTO(
        @NotNull List<UpdateItem> updates
) {
    public record UpdateItem(
            @NotNull SlotRequest selectedSlot,
            @NotNull RecipeSelectionDTO selectedRecipe
    ) {}

    public record SlotRequest(
            @NotNull MealType mealType,
            @NotNull DayOfWeek dayOfWeek
    ) {}
}
