package com.urisik.backend.domain.member.dto.res;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberResponse {


    @Getter
    @Builder
    public static class PatchAgree {
        private Boolean isSuccess;

        // 저장된 결과를 그대로 내려주면 프론트가 상태 렌더링하기 편함
        private Agreements agreements;

        // (선택) 회원가입 완료 여부를 내려주면 라우팅에 유용
        private Boolean signupCompleted;

    }


    @Getter
    @Builder
    public static class Agreements {
        private Boolean serviceTermsAgreed;
        private Boolean privacyPolicyAgreed;
        private Boolean familyInfoAgreed;
        private Boolean aiNoticeAgreed;
        private Boolean marketingOptIn;
    }
}
