package com.urisik.backend.domain.member.converter;

import com.urisik.backend.domain.member.dto.res.MemberResponse;
import com.urisik.backend.domain.member.entity.Member;

public class MemberConverter {

    public static MemberResponse.PatchAgree toPatchAgreeResponse(Member member) {

        boolean needAgreement =
                !member.isServiceTermsAgreed()
                        || !member.isPrivacyPolicyAgreed()
                        || !member.isFamilyInfoAgreed()
                        || !member.isAiNoticeAgreed();

        return MemberResponse.PatchAgree.builder()
                .serviceTermsAgreed(member.isServiceTermsAgreed())
                .privacyPolicyAgreed(member.isPrivacyPolicyAgreed())
                .familyInfoAgreed(member.isFamilyInfoAgreed())
                .aiNoticeAgreed(member.isAiNoticeAgreed())
                .marketingOptIn(member.isMarketingOptIn())
                .needAgreement(needAgreement)
                .build();
    }
}
