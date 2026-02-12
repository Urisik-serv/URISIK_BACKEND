package com.urisik.backend.domain.mealplan.exception;

import com.urisik.backend.domain.mealplan.exception.code.MealPlanErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;

public class MealPlanException extends GeneralException {
    public MealPlanException(MealPlanErrorCode code) {super(code);}
}
