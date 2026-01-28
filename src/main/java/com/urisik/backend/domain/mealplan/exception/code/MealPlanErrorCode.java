package com.urisik.backend.domain.mealplan.exception.code;

import com.urisik.backend.global.apiPayload.code.BaseErrorCode;
import com.urisik.backend.global.apiPayload.code.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MealPlanErrorCode implements BaseErrorCode {

    MEAL_PLAN_VALIDATION_FAILED(HttpStatus.BAD_REQUEST,
            "MEAL_PLAN_VALIDATION_400",
            "식단 생성 결과 검증에 실패했습니다."),

    MEAL_PLAN_ALREADY_EXISTS(HttpStatus.CONFLICT,
            "MEAL_PLAN_409",
            "이미 해당 주의 식단이 존재합니다."),

    MEAL_PLAN_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "MEAL_PLAN_GENERATION_500",
            "식단 생성 중 오류가 발생했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReason getReason() {
        return ErrorReason.builder()
                .httpStatus(httpStatus)
                .code(code)
                .message(message)
                .build();
    }
}
