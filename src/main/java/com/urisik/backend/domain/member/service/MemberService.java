package com.urisik.backend.domain.member.service;

import com.urisik.backend.domain.member.converter.MemberConverter;
import com.urisik.backend.domain.member.dto.req.MemberRequest;
import com.urisik.backend.domain.member.dto.res.MemberResponse;
import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.repo.MemberRepository;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse.PatchAgree patchAgree(Long memberId, MemberRequest.PatchAgree req) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenExcetion(AuthErrorCode.No_Member));

        // ✅ 저장
        member.setServiceTermsAgreed(req.getServiceTermsAgreed());
        member.setPrivacyPolicyAgreed(req.getPrivacyPolicyAgreed());
        member.setFamilyInfoAgreed(req.getFamilyInfoAgreed());
        member.setAiNoticeAgreed(req.getAiNoticeAgreed());
        member.setMarketingOptIn(req.getMarketingOptIn());

        // (영속 상태라 save 생략 가능, 명시하고 싶으면 save 해도 됨)
        // memberRepository.save(member);


        return MemberConverter.toPatchAgreeResponse(member);
    }
}