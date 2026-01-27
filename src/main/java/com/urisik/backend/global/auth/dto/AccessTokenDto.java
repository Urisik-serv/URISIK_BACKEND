package com.urisik.backend.global.auth.dto;

import lombok.Builder;

@Builder
public record AccessTokenDto(String accessToken,
                             boolean needAgreement,

                             boolean serviceTermsAgreed,
                             boolean privacyPolicyAgreed,
                             boolean familyInfoAgreed,
                             boolean aiNoticeAgreed,
                             boolean marketingOptIn) {
}
