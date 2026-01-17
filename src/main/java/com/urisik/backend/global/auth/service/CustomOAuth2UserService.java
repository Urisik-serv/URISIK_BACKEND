package com.urisik.backend.global.auth.service;

import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.repo.MemberRepository;
import com.urisik.backend.global.auth.dto.CustomOAuth2User;
import com.urisik.backend.global.auth.dto.MemberDto;
import com.urisik.backend.global.auth.dto.res.GoogleResponse;
import com.urisik.backend.global.auth.dto.res.KakaoResponse;
import com.urisik.backend.global.auth.dto.res.OAuth2Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override // userRequest에는 소셜 서버에서 준 정보가 담겨있음.
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("kakao")) {

            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {

            return null;
        }

        // provider 식별

        String memberCredential = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        Member existData = memberRepository.findByCredentialId(memberCredential);

        if (existData == null) {

            Member member = Member.builder()
                    .credentialId(memberCredential)
                    .name(oAuth2Response.getName())
                    .role("ROLE_USER")
                    .aiNoticeAgreed(false)
                    .familyInfoAgreed(false)
                    .marketingOptIn(false)
                    .privacyPolicyAgreed(false)
                    .serviceTermsAgreed(false)
                    .build();


            Member save = memberRepository.save(member);

            MemberDto memberDto = MemberDto.builder()
                    .id(save.getId())
                    .credentialId(save.getCredentialId())
                    .name(save.getName())
                    .role("ROLE_USER")
                    .build();

            return new CustomOAuth2User(memberDto);
        } else {

            existData.setName(oAuth2Response.getName());

            memberRepository.save(existData);

            MemberDto memberDto = MemberDto.builder()
                    .id(existData.getId())
                    .credentialId(existData.getCredentialId())
                    .name(oAuth2Response.getName())
                    .role(existData.getRole())
                    .build();

            return new CustomOAuth2User(memberDto);
        }
    }
}