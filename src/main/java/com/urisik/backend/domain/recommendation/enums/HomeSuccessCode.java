package com.urisik.backend.domain.recommendation.enums;

import com.urisik.backend.global.apiPayload.code.BaseSuccessCode;
import com.urisik.backend.global.apiPayload.code.SuccessReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HomeSuccessCode implements BaseSuccessCode {


    HOME_SAFE_RECIPE_OK(HttpStatus.OK, "HOME_200_001", "홈 안전 레시피 추천 성공"),
    RECOMMEND_HIGH_SCORE_OK(HttpStatus.OK, "HOME_200_002", "홈 평점 순 레시피 추천 성공"),
    RECOMMEND_SAFE_HIGH_SCORE_OK(HttpStatus.OK, "HOME_200_003", "홈 평점 순 레시피 추천 성공"),
    RECOMMEND_WISH_HIGH_SCORE_OK(HttpStatus.OK, "HOME_200_004", "홈 위시리스트 순 레시피 추천 성공");
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

