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

    MEAL_PLAN_RECIPE_NOT_FOUND(HttpStatus.BAD_REQUEST,
            "MEAL_PLAN_RECIPE_NOT_FOUND_400",
            "레시피가 존재하지 않습니다."),

    MEAL_PLAN_TRANSFORMED_RECIPE_NOT_FOUND(HttpStatus.BAD_REQUEST,
            "MEALPLAN_400",
            "해당 레시피에 대한 우리 가족 변형 레시피가 존재하지 않습니다."),

    MEAL_PLAN_SLOT_INVALID(HttpStatus.BAD_REQUEST,
            "MEAL_PLAN_401",
            "요청한 슬롯 정보가 올바르지 않습니다."),

    MEAL_PLAN_SLOT_NOT_SELECTED(HttpStatus.BAD_REQUEST,
            "MEAL_PLAN_402",
            "선택되지 않은 슬롯은 수정할 수 없습니다."),

    MEAL_PLAN_RECIPE_NOT_SAFE(HttpStatus.BAD_REQUEST,
            "MEAL_PLAN_403",
            "가족 기준으로 안전하지 않은 레시피입니다."),

    MEAL_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND,
            "MEAL_PLAN_404",
            "식단을 찾을 수 없습니다."),

    MEAL_PLAN_NOT_IN_FAMILY_ROOM(HttpStatus.NOT_FOUND,
            "MEAL_PLAN_405",
            "해당 가족방의 식단이 아닙니다."),

    MEAL_PLAN_ALREADY_EXISTS(HttpStatus.CONFLICT,
            "MEAL_PLAN_409",
            "이미 해당 주의 식단이 존재합니다."),

    MEAL_PLAN_ALREADY_CONFIRMED(HttpStatus.CONFLICT,
            "MEAL_PLAN_410",
            "이미 확정된 식단입니다."),

    MEAL_PLAN_NOT_DRAFT(HttpStatus.CONFLICT,
            "MEAL_PLAN_411",
            "확정된 식단은 수정할 수 없습니다."),

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
