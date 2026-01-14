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
            "이미 있는 가족명입니다.")
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
