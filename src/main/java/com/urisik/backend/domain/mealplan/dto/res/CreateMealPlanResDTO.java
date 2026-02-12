package com.urisik.backend.domain.mealplan.dto.res;

import com.urisik.backend.domain.mealplan.dto.common.RecipeDTO;
import com.urisik.backend.domain.mealplan.enums.MealPlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class CreateMealPlanResDTO {
    private Long mealPlanId;
    private Long familyRoomId;
    private LocalDate weekStartDate;
    private MealPlanStatus status;
    private Map<String, RecipeDTO> slots;
}
