package com.urisik.backend.domain.recipe.enums;


import com.urisik.backend.global.apiPayload.code.BaseSuccessCode;
import com.urisik.backend.global.apiPayload.code.SuccessReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecipeSuccessCode implements BaseSuccessCode {

    RECIPE_SEARCH_OK(HttpStatus.OK, "RECIPE_200_001", "레시피 검색 성공"),
    RECIPE_DETAIL_OK(HttpStatus.OK, "RECIPE_200_002", "레시피 상세 조회 성공");

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

