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
    TRANSFORMED_RECIPE_NOT_FOUND(HttpStatus.BAD_GATEWAY, "RECIPE_404_002", "변형 레시피를 찾을 수 없습니다."),
    EXTERNAL_RECIPE_NOT_FOUND(HttpStatus.BAD_GATEWAY, "RECIPE_404_003", "외부 레시피를 찾을 수 없습니다.");


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
