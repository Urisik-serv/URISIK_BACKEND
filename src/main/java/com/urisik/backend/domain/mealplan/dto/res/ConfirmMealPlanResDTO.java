package com.urisik.backend.domain.mealplan.dto.res;

import com.urisik.backend.domain.mealplan.enums.MealPlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class ConfirmMealPlanResDTO {
    private Long mealPlanId;
    private MealPlanStatus status;
    private LocalDate weekStartDate;
    private Integer mealPlanGenerationCount;
}
