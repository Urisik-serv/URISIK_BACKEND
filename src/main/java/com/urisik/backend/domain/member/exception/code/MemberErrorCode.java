package com.urisik.backend.domain.member.exception.code;

import com.urisik.backend.global.apiPayload.code.BaseErrorCode;
import com.urisik.backend.global.apiPayload.code.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {


    WISH_NOT_FOUND(HttpStatus.GONE,
            "AUTH_200",
            "요청한 레시피 위시 중 일부가 위시리스트에 존재하지 않음"),
    WISH_ALREADY_IN(HttpStatus.GONE,
            "AUTH_200",
            "요청한 레시피 위시 중 일부가 이미 위시리스트에 존재합니다."),
    TRANS_WISH_NOT_FOUND(HttpStatus.GONE,
            "AUTH_200",
            "요청한 변형된 레시피 위시 중 일부가 위시리스트에 존재하지 않음"),
    TRANS_WISH_ALREADY_IN(HttpStatus.GONE,
            "AUTH_200",
            "요청한 변형 레시피 위시 중 일부가 이미 위시리스트에 존재합니다."),

    INVALID_FILE(HttpStatus.GONE,
            "AUTH_200",
            "파일이 비어있습니다."),

    INVALID_FILE_TYPE(HttpStatus.GONE,
            "AUTH_200",
            "IMAGE 파일이 아닙니다."),

    NO_ROLES(HttpStatus.GONE,
            "AUTH_200",
            "가족내 해당 구성원이 없습니다."),

    NO_ROOM(HttpStatus.GONE,
            "AUTH_200",
            "회원님이 속한 가족방이 없습니다."),

    FORBIDDEN_ROOM(HttpStatus.GONE,
            "AUTH_200",
            "권한이 없는 요청입니다."),

    FORBIDDEN_MEMBER(HttpStatus.GONE,
            "AUTH_200",
            "권한이 없는 요청입니다."),

    NO_TOKEN(HttpStatus.GONE,
            "AUTH_200",
            "인증되지 않은 사용자 입니다"),

    ALREADY_EXIST_ROLE(HttpStatus.GONE,
            "AUTH_200",
            "해당 역할은 이미 등록되어 있습니다."),

    NO_PROFILE_IN_FAMILY(HttpStatus.GONE,
            "AUTH_200",
            "가족방 안의 유저의 프로필이 없습니다."),
    NO_RECIPE(HttpStatus.GONE,
            "AUTH_200",
            "해당 레시피가 없습니다."),
    ALREADY_HAVE_PROFILE(HttpStatus.GONE,
            "AUTH_200",
            "해당하는 사용자가 없습니다."),
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
