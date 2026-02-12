package com.urisik.backend.global.auth.exception.code;

import com.urisik.backend.global.apiPayload.code.BaseErrorCode;
import com.urisik.backend.global.apiPayload.code.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    NO_HEADER(HttpStatus.BAD_REQUEST,
            "AUTH_400",
            "요청에 인증 헤더가 없습니다."),

    NOT_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,
            "AUTH_401",
            "리프레시 토큰이 아닙니다."),

    TOKEN_NOT_VALID(HttpStatus.UNAUTHORIZED,
            "AUTH_401",
            "토큰이 유효하지 않습니다."),

    TYPE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "AUTH_500_TYPE_ERROR",
            "서버 내부 오류"),

    NO_TOKEN(HttpStatus.UNAUTHORIZED,
            "AUTH_401",
            "인증되지 않은 사용자입니다."),

    NO_MEMBER(HttpStatus.NOT_FOUND,
            "MEMBER_404",
            "해당 사용자가 없습니다.");



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
