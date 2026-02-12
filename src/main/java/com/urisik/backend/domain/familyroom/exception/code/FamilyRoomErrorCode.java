package com.urisik.backend.domain.familyroom.exception.code;

import com.urisik.backend.global.apiPayload.code.BaseErrorCode;
import com.urisik.backend.global.apiPayload.code.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FamilyRoomErrorCode implements BaseErrorCode {

    FAMILY_ROOM(HttpStatus.BAD_REQUEST,
            "FAMILY_ROOM_400",
            "가족방 요청이 올바르지 않습니다."),

    INVITE_FORBIDDEN(HttpStatus.FORBIDDEN,
            "INVITE_403",
            "초대 토큰 생성 권한이 없습니다."),

    NOT_LEADER(HttpStatus.FORBIDDEN,
            "FAMILY_ROOM_403",
            "가족방 방장 권한이 없습니다."),

    NOT_FAMILY_MEMBER(HttpStatus.FORBIDDEN,
            "FAMILY_ROOM_403",
            "가족방 구성원이 아닙니다." ),

    FAMILY_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND,
            "FAMILY_ROOM_404",
            "가족방을 찾을 수 없습니다."),

    FAMILY_WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND,
            "FAMILY_WISHLIST_404",
            "가족 위시리스트 항목을 찾을 수 없습니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "MEMBER_404",
            "회원 정보를 찾을 수 없습니다."),

    INVITE_TOKEN_INVALID(HttpStatus.NOT_FOUND,
            "INVITE_404",
            "유효하지 않은 초대 링크입니다."),

    FAMILY_MEMBER_ALREADY_JOINED(HttpStatus.CONFLICT,
            "FAMILY_JOIN_409",
            "이미 가족방에 참여한 사용자입니다."),

    INVITE_TOKEN_EXPIRED(HttpStatus.GONE,
            "INVITE_410",
            "만료된 초대 링크입니다.")
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
