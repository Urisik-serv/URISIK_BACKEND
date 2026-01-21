package com.urisik.backend.domain.member.exception.code;

import com.urisik.backend.global.apiPayload.code.BaseErrorCode;
import com.urisik.backend.global.apiPayload.code.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {


    No_Roles(HttpStatus.GONE,
            "Auth_200",
            "가족내 해당 구성원이 없습니다."),
    Not_Refresh_Token(HttpStatus.GONE,
            "Auth_200",
            "리프레시 토큰이 아닙니다."),

    Token_Not_Vaild(HttpStatus.GONE,
            "Auth_200",
            "토큰이 유효하지 않습니다."),
    Type_Error(HttpStatus.GONE,
            "Auth_200",
            "서버 내 오류"),
    No_Token(HttpStatus.GONE,
            "Auth_200",
            "인증되지 않은 사용자 입니다"),

    No_Member(HttpStatus.GONE,
            "Auth_200",
            "해당하는 사용자가 없습니다.")
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
