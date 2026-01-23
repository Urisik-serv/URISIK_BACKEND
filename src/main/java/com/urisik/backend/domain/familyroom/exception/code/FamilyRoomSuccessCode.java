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

    FAMILY_ROOM_UPDATED(HttpStatus.OK,
            "FAMILY_ROOM_UPDATED_200",
            "가족방 수정 성공"),

    FAMILY_ROOM_DELETED(HttpStatus.OK,
            "FAMILY_ROOM_DELETED_200",
            "가족방 삭제 성공"),

    FAMILY_MEMBER_ADDED(HttpStatus.OK,
            "FAMILY_MEMBER_200",
            "가족 구성원 추가 성공"),

    FAMILY_MEMBER_REMOVED(HttpStatus.OK,
            "FAMILY_MEMBER_REMOVED_200",
            "가족 구성원 삭제 성공"),

    INVITE_CREATED(HttpStatus.OK,
            "INVITE_200",
            "초대 토큰 생성 성공"),

    INVITE_PREVIEW(HttpStatus.OK,
            "INVITE_200",
            "초대 링크 조회 성공"),

    FAMILY_JOIN(HttpStatus.OK,
            "FAMILY_JOIN_200",
            "가족방 참여 성공"),

    FAMILY_ROOM_CONTEXT(HttpStatus.OK,
            "FAMILY_ROOM_CONTEXT_200",
            "가족방 컨텍스트 조회 성공");

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
