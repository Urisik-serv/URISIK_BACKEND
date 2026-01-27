package com.urisik.backend.domain.member.dto.req;

import com.urisik.backend.domain.member.enums.AlarmPolicy;
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
        // 필수 동의 4개 (서비스 정책에 맞춰 필수/선택 조절)
        @NotNull private Boolean serviceTermsAgreed;
        @NotNull private Boolean privacyPolicyAgreed;
        @NotNull private Boolean familyInfoAgreed;
        @NotNull private Boolean aiNoticeAgreed;

        // 선택 동의
        @NotNull private Boolean marketingOptIn;


    }
    @Getter
    @NoArgsConstructor
    public static class AlarmUpdateInfo {
        @NotNull private AlarmPolicy alarmPolicy;
    }

}
