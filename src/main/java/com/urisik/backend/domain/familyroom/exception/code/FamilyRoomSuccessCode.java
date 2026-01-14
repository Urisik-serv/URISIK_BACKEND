package com.urisik.backend.domain.familyroom.exception.code;

import com.urisik.backend.global.apiPayload.code.BaseSuccessCode;
import com.urisik.backend.global.apiPayload.code.SuccessReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FamilyRoomSuccessCode implements BaseSuccessCode {

    FAMILY_ROOM(HttpStatus.OK,
            "FAMILY_ROOM_200",
            "가족방 생성 성공"),
    ;

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
