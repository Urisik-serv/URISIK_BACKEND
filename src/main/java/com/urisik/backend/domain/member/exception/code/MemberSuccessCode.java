package com.urisik.backend.domain.member.exception.code;

import com.urisik.backend.global.apiPayload.code.BaseSuccessCode;
import com.urisik.backend.global.apiPayload.code.SuccessReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseSuccessCode {



    MEMBER_PROFILE_GET(HttpStatus.GONE,
            "Auth_200_2",
            "프로필 요청 성공했습니다."),

    MEMBER_PROFILE_CREATE(HttpStatus.GONE,
            "Auth_200_2",
            "프로필 생성 성공했습니다."),

    MEMBER_PROFILE_UPDATE(HttpStatus.GONE,
            "Auth_200_2",
            "프로필 수정 성공했습니다."),

    MEMBER_PROFILE_DELETE(HttpStatus.GONE,
            "Auth_200_2",
            "가족방 탈퇴 성공했습니다."),

    MEMBER_PROFILE_PIC_UPDATE(HttpStatus.GONE,
            "Auth_200_2",
            "프로필사진이 변경되었습니다"),



    AlARM_GET(HttpStatus.GONE,
            "Auth_200_2",
            "알람 동의 여부 조회 성공."),
    AlARM_UPDATE(HttpStatus.GONE,
            "Auth_200_2",
            "알람 동의 여부 변경 성공."),
    AGREEMENT_UPDATE(HttpStatus.GONE,
            "Auth_200_2",
            "사용자 약관 동의 여부 갱신 성공"),

    WISH_LIST_CREATE(HttpStatus.OK,
            "WISH_200_1",
            "위시리스트 등록 성공"),

    WISH_LIST_GET(HttpStatus.OK,
            "WISH_200_2",
            "위시리스트 조회 성공"),

    WISH_LIST_DELETE(HttpStatus.OK,
            "WISH_200_3",
            "위시리스트 삭제 성공")

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
