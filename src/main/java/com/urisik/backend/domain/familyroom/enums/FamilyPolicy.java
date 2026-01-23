package com.urisik.backend.domain.familyroom.enums;

import com.urisik.backend.domain.member.enums.FamilyRole;

public enum FamilyPolicy {
    MOTHER_ONLY("엄마"),
    FATHER_ONLY("아빠");

    private final String koreanName;

    FamilyPolicy(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    /**
     * isLeaderRole
     * 가족방의 정책(FamilyPolicy) 기준으로, 주어진 프로필 역할(FamilyRole)이 방장에 해당하는지 판단
     * 방장 권한은 상태가 아니라, 규칙으로 정해짐 (방 생성 -> 프로필 작성)
     * - 권한을 저장하지 않고 role + policy 조합으로 항상 계산
     * - 프로필이 없거나(role == null) 방장 조건에 맞지 않으면 false
     * - 정책 변경 시 이 메서드만 수정하면 전체 권한 판단이 일관되게 반영
     */
    public boolean isLeaderRole(FamilyRole role) {
        if (role == null) return false;

        return switch (this) {
            case MOTHER_ONLY -> role == FamilyRole.MOM;
            case FATHER_ONLY -> role == FamilyRole.DAD;
        };
    }
}
