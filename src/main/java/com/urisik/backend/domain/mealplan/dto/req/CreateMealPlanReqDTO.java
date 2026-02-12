package com.urisik.backend.domain.mealplan.dto.req;

import com.urisik.backend.domain.mealplan.enums.MealType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record CreateMealPlanReqDTO(
        @NotNull LocalDate weekStartDate,
        @NotEmpty List<SlotRequest> selectedSlots,
        boolean regenerate
) {
    public record SlotRequest(
            @NotNull MealType mealType,
            @NotNull DayOfWeek dayOfWeek
    ) {}
}
