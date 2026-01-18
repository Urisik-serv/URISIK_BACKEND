package com.urisik.backend.domain.mealplan.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MealType {

    LUNCH("점심"),
    DINNER("저녁");

    private final String label;
}
