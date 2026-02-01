package com.urisik.backend.domain.allergy.enums;

import com.urisik.backend.global.apiPayload.code.BaseSuccessCode;
import com.urisik.backend.global.apiPayload.code.SuccessReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AllergySuccessCode implements BaseSuccessCode {

    ALLERGY_LIST_OK(
            HttpStatus.OK,
            "ALLERGY_200",
            "사용자 알레르기 조회 성공"
    ),

    FAMILY_ALLERGY_LIST_OK(
            HttpStatus.OK,
            "ALLERGY_201",
            "가족방 알레르기 조회 성공"
    ),

    RECIPE_ALLERGY_CHECK_OK(
            HttpStatus.OK,
            "ALLERGY_200_002",
            "레시피 알레르기 판별 및 대체 식재료 조회 성공"
    );

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

