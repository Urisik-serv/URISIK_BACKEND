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
            @NotNull RecipeRefDTO selectedRecipe
    ) {}

    /**
     * 수정 요청에서 사용할 레시피 참조 DTO
     * - title/baseRecipeId 등 파생 데이터는 서버가 조회하여 결정
     */
    public record RecipeRefDTO(
            @NotNull RecipeType type,
            @NotNull Long id
    ) {
        public enum RecipeType {
            RECIPE,
            TRANSFORMED_RECIPE
        }
    }

    public record SlotRequest(
            @NotNull MealType mealType,
            @NotNull DayOfWeek dayOfWeek
    ) {}
}
