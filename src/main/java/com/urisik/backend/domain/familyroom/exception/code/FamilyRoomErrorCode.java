package com.urisik.backend.domain.familyroom.exception.code;

import com.urisik.backend.global.apiPayload.code.BaseErrorCode;
import com.urisik.backend.global.apiPayload.code.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FamilyRoomErrorCode implements BaseErrorCode {

    FAMILY_ROOM(HttpStatus.OK,
            "FAMILY_ROOM_400",
            "가족방 생성 요청이 올바르지 않습니다."),

    FAMILY_NAME_DUPLICATED(HttpStatus.CONFLICT,
            "FAMILY_ROOM_409",
            "이미 있는 가족명입니다."),

    INVITE_FORBIDDEN(HttpStatus.FORBIDDEN,
            "INVITE_403",
            "초대 토큰 생성 권한이 없습니다."),

    INVITE_TOKEN_INVALID(HttpStatus.NOT_FOUND,
            "INVITE_404",
            "유효하지 않은 초대 링크입니다."),

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

    /**
     * Invite 도메인 에러 코드
     */
    public enum InviteErrorCode {
        INVITE_FORBIDDEN,
        INVITE_TOKEN_INVALID,
        INVITE_TOKEN_EXPIRED
    }
}
