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
    private String updatedSlotKey;
    private RecipeDTO recipe;
}
