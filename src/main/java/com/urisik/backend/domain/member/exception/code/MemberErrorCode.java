package com.urisik.backend.domain.member.exception.code;

import com.urisik.backend.global.apiPayload.code.BaseErrorCode;
import com.urisik.backend.global.apiPayload.code.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {


    WISH_NOT_FOUND(HttpStatus.NOT_FOUND,
            "WISH_404",
            "요청한 레시피 위시가 존재하지 않습니다."),
    NO_REVIEW(HttpStatus.NOT_FOUND,
            "WISH_404",
            "4점 이상의 리뷰가 없어서 추천이 없습니다."),

    WISH_ALREADY_IN(HttpStatus.CONFLICT,
            "WISH_409",
            "이미 위시리스트에 존재합니다."),

    TRANS_WISH_NOT_FOUND(HttpStatus.NOT_FOUND,
            "TRANS_WISH_404",
            "변형 레시피 위시가 존재하지 않습니다."),

    TRANS_WISH_ALREADY_IN(HttpStatus.CONFLICT,
            "TRANS_WISH_409",
            "이미 변형 레시피 위시가 존재합니다."),

    NOT_YOUR_ROOM(HttpStatus.FORBIDDEN,
            "ROOM_403",
            "해당 가족방에 대한 권한이 없습니다."),

    INVALID_FILE(HttpStatus.BAD_REQUEST,
            "FILE_400",
            "파일이 비어있습니다."),

    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST,
            "FILE_400_TYPE",
            "이미지 파일이 아닙니다."),

    NO_ROLES(HttpStatus.NOT_FOUND,
            "ROLE_404",
            "가족 내 해당 구성원이 없습니다."),

    NO_ROOM(HttpStatus.NOT_FOUND,
            "ROOM_404",
            "회원님이 속한 가족방이 없습니다."),

    NO_TRANS_RECIPE(HttpStatus.NOT_FOUND,
            "TRANS_RECIPE_404",
            "요청한 변형 레시피가 없습니다."),

    FORBIDDEN_ROOM(HttpStatus.FORBIDDEN,
            "ROOM_403",
            "가족방 접근 권한이 없습니다."),

    FORBIDDEN_MEMBER(HttpStatus.FORBIDDEN,
            "MEMBER_403",
            "사용자 권한이 없습니다."),

    ALREADY_EXIST_ROLE(HttpStatus.CONFLICT,
            "ROLE_409",
            "해당 역할은 이미 등록되어 있습니다."),

    NO_PROFILE_IN_FAMILY(HttpStatus.NOT_FOUND,
            "PROFILE_404",
            "가족방 내 프로필이 없습니다."),

    NO_RECIPE(HttpStatus.NOT_FOUND,
            "RECIPE_404",
            "요청한 레시피가 없습니다."),

    ALREADY_HAVE_PROFILE(HttpStatus.CONFLICT,
            "PROFILE_409",
            "이미 프로필이 존재합니다."),

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
