package com.urisik.backend.domain.familyroom.enums;

public enum FamilyRole {
    MOM("엄마"),
    DAD("아빠"),
    SON("아들"),
    DAUGHTER("딸");

    private final String koreanName;

    FamilyRole(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
