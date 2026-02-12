package com.urisik.backend.domain.mealplan.dto.event;

public record MealPlanConfirmedEvent(
        Long familyRoomId,
        Integer mealPlanGenerationCount
) {
}
