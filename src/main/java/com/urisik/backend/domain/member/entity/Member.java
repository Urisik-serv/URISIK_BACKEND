package com.urisik.backend.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "member")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //소셜정보 + " " + 소셜 provider 값
    @Column(name = "member_credential", nullable = false, unique = true)
    private String credentialId;

    @Column(name = "member_name", nullable = false)
    private String name;

    // 일반 유저: ROLE_USER
    @Column (name = "member_role", nullable = false)
    private String role;


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

