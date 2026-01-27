package com.urisik.backend.domain.member.dto.req;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class MemberRequest {

    @Getter
    @NoArgsConstructor
    public static class PatchAgree {
        // ✅ 필수 약관들
        @NotNull @AssertTrue(message = "서비스 이용약관 동의는 필수입니다.")
        private Boolean serviceTermsAgreed;

        @NotNull @AssertTrue(message = "개인정보 처리방침 동의는 필수입니다.")
        private Boolean privacyPolicyAgreed;

        @NotNull @AssertTrue(message = "아동/가족 정보 조항 동의는 필수입니다.")
        private Boolean familyInfoAgreed;

        @NotNull @AssertTrue(message = "AI 추천 고지 동의는 필수입니다.")
        private Boolean aiNoticeAgreed;

        // ✅ 선택 약관
        @NotNull
        private Boolean marketingOptIn;


    }
}
