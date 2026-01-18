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

