package com.urisik.backend.domain.member.enums;

public enum FamilyRole {
    MOM("엄마"),
    DAD("아빠"),
    SON("아들"),
    GRANDPA("할아버지"),
    GRANDMA("할머니"),
    DAUGHTER("딸");

    private final String koreanName;

    FamilyRole(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
