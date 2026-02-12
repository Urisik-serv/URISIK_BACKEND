package com.urisik.backend.domain.member.dto.res;

import com.urisik.backend.domain.member.enums.AlarmPolicy;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberResponse {


    @Getter
    @Builder
    public static class PatchAgree {


        private boolean serviceTermsAgreed;
        private boolean privacyPolicyAgreed;
        private boolean familyInfoAgreed;
        private boolean aiNoticeAgreed;
        private boolean marketingOptIn;

        // 이제 “동의 더 필요한지” 프론트가 바로 판단 가능
        private boolean needAgreement;
    }

    @Getter
    @Builder
    public static class alarmInfo {
        private AlarmPolicy alarmPolicy;
    }
}
