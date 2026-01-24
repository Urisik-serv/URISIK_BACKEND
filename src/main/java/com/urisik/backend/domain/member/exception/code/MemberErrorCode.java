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
    No_Room(HttpStatus.GONE,
            "Auth_200",
            "회원님이 속한 가족방이 없습니다."),
    FORBIDDEN_ROOM(HttpStatus.GONE,
            "Auth_200",
            "권한이 없는 요청입니다."),
    No_Token(HttpStatus.GONE,
            "Auth_200",
            "인증되지 않은 사용자 입니다"),
    Already_Exist_Role(HttpStatus.GONE,
            "Auth_200",
            "해당 역할은 이미 등록되어 있습니다."),
    No_Profile_In_Family(HttpStatus.GONE,
            "Auth_200",
            "가족방 안의 유저의 프로필이 없습니다."),

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
