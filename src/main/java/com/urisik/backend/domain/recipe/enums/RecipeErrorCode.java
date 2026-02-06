package com.urisik.backend.domain.recipe.enums;

import com.urisik.backend.global.apiPayload.code.BaseErrorCode;
import com.urisik.backend.global.apiPayload.code.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecipeErrorCode implements BaseErrorCode {

    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECIPE_404_001", "해당 음식에 대한 레시피를 찾을 수 없습니다."),
    RECIPE_INVALID_KEY(HttpStatus.BAD_REQUEST, "RECIPE_400_001", "유효하지 않은 recipeKey 입니다."),
    RECIPE_EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "RECIPE_502_001", "외부 레시피 API 호출에 실패했습니다."),
    TRANSFORMED_RECIPE_NOT_FOUND(HttpStatus.BAD_GATEWAY, "RECIPE_404_002", "변형 레시피를 찾을 수 없습니다."),
    EXTERNAL_RECIPE_NOT_FOUND(HttpStatus.BAD_GATEWAY, "RECIPE_404_003", "외부 레시피를 찾을 수 없습니다."),
    RECIPE_NO_ALLERGY_RISK(HttpStatus.BAD_GATEWAY, "RECIPE_404_004", "알레르기 대체가 필요 없는 레시피입니다."),
    ALLERGY_REPLACEMENT_NOT_FOUND(HttpStatus.BAD_GATEWAY, "RECIPE_404_005", "레시피에 대체할 알레르기가 없습니다"),
    AI_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "RECIPE_404_006", "AI 생성에 실패했습니다");

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
