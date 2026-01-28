package com.urisik.backend.global.auth.exception.code;

import com.urisik.backend.global.apiPayload.code.BaseErrorCode;
import com.urisik.backend.global.apiPayload.code.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    NO_HEADER(HttpStatus.GONE,
            "AUTH_200",
            "요청에 헤더가 없습니다."),

    NOT_REFRESH_TOKEN(HttpStatus.GONE,
            "AUTH_200",
            "리프레시 토큰이 아닙니다."),

    TOKEN_NOT_VALID(HttpStatus.GONE,
            "AUTH_200",
            "토큰이 유효하지 않습니다."),

    TYPE_ERROR(HttpStatus.GONE,
            "AUTH_200",
            "서버 내부 오류"),

    NO_TOKEN(HttpStatus.GONE,
            "AUTH_200",
            "인증되지 않은 사용자 입니다."),

    NO_MEMBER(HttpStatus.GONE,
            "AUTH_200",
            "해당하는 사용자가 없습니다.");



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
