package com.urisik.backend.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_credential", nullable = false)
    private String memberCredential;

    @Column(name = "member_name", nullable = false)
    private String memberName;


    // ✅ 약관 동의 여부 (필수/선택)
    @Column(name = "service_terms_agreed", nullable = false)
    private boolean serviceTermsAgreed;

    @Column(name = "privacy_policy_agreed", nullable = false)
    private boolean privacyPolicyAgreed;

    @Column(name = "family_info_agreed", nullable = false)
    private boolean familyInfoAgreed;

    @Column(name = "ai_notice_agreed", nullable = false)
    private boolean aiNoticeAgreed;

    @Column(name = "marketing_opt_in", nullable = false)
    private boolean marketingOptIn;



}

