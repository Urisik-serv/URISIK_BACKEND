package com.urisik.backend.domain.mealplan.dto.res;

import com.urisik.backend.domain.mealplan.dto.common.RecipeDTO;
import com.urisik.backend.domain.mealplan.enums.MealPlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateMealPlanResDTO {
    private Long mealPlanId;
    private MealPlanStatus status;
    private java.util.List<UpdatedSlot> updatedSlots;

    public static UpdateMealPlanResDTO bulk(
            Long mealPlanId,
            MealPlanStatus status,
            java.util.List<UpdatedSlot> updatedSlots
    ) {
        return UpdateMealPlanResDTO.builder()
                .mealPlanId(mealPlanId)
                .status(status)
                .updatedSlots(updatedSlots)
                .build();
    }

    @Getter
    @AllArgsConstructor
    public static class UpdatedSlot {
        private String slotKey;   // e.g. DINNER-MONDAY
        private RecipeDTO recipe;
    }
}
