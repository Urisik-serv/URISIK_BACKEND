package com.urisik.backend.domain.member.exception.code;

import com.urisik.backend.global.apiPayload.code.BaseSuccessCode;
import com.urisik.backend.global.apiPayload.code.SuccessReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseSuccessCode {


    Login_Access_Token(HttpStatus.GONE,
            "Auth_200_1",
            "Access 토큰 발급 성공했습니다."),
    Logout_Suc(HttpStatus.GONE,
            "Auth_200_2",
            "로그아웃 성공했습니다."),
    Auth_delete_Suc(HttpStatus.GONE,
            "Auth_200_2",
            "계정이 성공적으로 삭제 되었습니다."),
    MemberProfile_Get(HttpStatus.GONE,
            "Auth_200_2",
            "프로필 요청 성공했습니다."),
    MemberProfile_Create(HttpStatus.GONE,
            "Auth_200_2",
            "프로필 생성 성공했습니다."),
    MemberProfile_Update(HttpStatus.GONE,
            "Auth_200_2",
            "프로필 수정 성공했습니다."),
    MemberProfile_Delete(HttpStatus.GONE,
            "Auth_200_2",
            "가족방 탈퇴 성공했습니다.")


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
