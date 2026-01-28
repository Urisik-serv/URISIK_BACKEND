package com.urisik.backend.domain.mealplan.exception.code;

import com.urisik.backend.global.apiPayload.code.BaseSuccessCode;
import com.urisik.backend.global.apiPayload.code.SuccessReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MealPlanSuccessCode implements BaseSuccessCode {

    MEAL_PLAN_CREATED(HttpStatus.OK,
            "MEAL_PLAN_CREATED_200",
            "주간 식단 생성에 성공했습니다."),

    MEAL_PLAN_CONFIRMED(HttpStatus.OK,
            "MEAL_PLAN_CONFIRMED_200",
            "주간 식단 확정에 성공했습니다."),

    MEAL_PLAN_REGENERATED(HttpStatus.OK,
            "MEAL_PLAN_REGENERATED_200",
            "주간 식단 재생성에 성공했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public SuccessReason getReason() {
        return SuccessReason.builder()
                .httpStatus(httpStatus)
                .code(code)
                .message(message)
                .build();
    }
}
