package com.urisik.backend.domain.allergy.enums;


import java.util.Arrays;

public enum Allergen {

        EGG("달걀"),
        MILK("우유"),
        BUCKWHEAT("메밀"),
        PEANUT("땅콩"),
        SOY("대두"),
        WHEAT("밀"),
        MACKEREL("고등어"),
        CRAB("게"),
        SHRIMP("새우"),
        PORK("돼지고기"),
        PEACH("복숭아"),
        TOMATO("토마토"),
        SULFITES("아황산류"),
        WALNUT("호두"),
        CHICKEN("닭고기"),
        BEEF("쇠고기"),
        SQUID("오징어"),
        OYSTER("굴"),
        ABALONE("전복"),
        MUSSEL("홍합"),
        PINE_NUT("잣"),
        EXTRACTED_INGREDIENTS("추출성분(젤라틴 등)");

        private final String koreanName;

        Allergen(String koreanName) {
                this.koreanName = koreanName;
        }

        public String getKoreanName() {
                return koreanName;
        }

        public static Allergen from(String name) {
                return Arrays.stream(values())
                        .filter(a -> a.name().equalsIgnoreCase(name))
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException("Unknown allergen: " + name));
        }


        public static Allergen fromKorean(String koreanName) {
                return Arrays.stream(values())
                        .filter(a -> a.koreanName.equals(koreanName))
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException("Unknown allergen (korean): " + koreanName));
        }

}


