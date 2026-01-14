package com.urisik.backend.domain.member.entity;

import com.urisik.backend.domain.Allergy;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;




@Entity
@Table(name = "member_allergy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAllergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N : 1 (여러 알레르기 정보가 한 가족회원프로필에 속함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_profile_id", nullable = false) // <- DB 컬럼명 맞춰 수정
    private FamilyMemberProfile familyMemberProfile;

    // N : 1 (여러 회원알레르기정보가 한 알레르기 타입을 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allergy_id", nullable = false) // <- DB 컬럼명 맞춰 수정
    private Allergy allergy;


}