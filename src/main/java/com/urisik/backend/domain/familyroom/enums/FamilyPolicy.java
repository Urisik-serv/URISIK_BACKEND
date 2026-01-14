package com.urisik.backend.domain.familyroom.enums;

public enum FamilyPolicy {
    BOTH_PARENTS("공동"),
    MOTHER_ONLY("엄마"),
    FATHER_ONLY("아빠");

    private final String koreanName;

    FamilyPolicy(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
