package com.urisik.backend.domain.mealplan.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MealPlanStatus {

    DRAFT("임시"),
    CONFIRMED("확정");

    private final String label;
}
