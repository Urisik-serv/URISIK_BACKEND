package com.urisik.backend.domain.allergy.enums;


import java.util.Arrays;

import java.util.*;
import java.util.stream.Collectors;

public enum Allergen {

        EGG("달걀", Set.of("달걀", "계란", "난백", "난황")),
        MILK("우유", Set.of("우유", "생크림", "치즈", "버터", "연유", "요거트")),
        BUCKWHEAT("메밀", Set.of("메밀")),
        PEANUT("땅콩", Set.of("땅콩", "피넛")),
        SOY("대두", Set.of("대두", "콩", "두부", "된장", "간장")),
        WHEAT("밀", Set.of("밀", "밀가루", "글루텐")),
        MACKEREL("고등어", Set.of("고등어")),
        CRAB("게", Set.of("게")),
        SHRIMP("새우", Set.of("새우", "대하")),
        PORK("돼지고기", Set.of("돼지고기", "돼지", "삼겹살", "목살")),
        PEACH("복숭아", Set.of("복숭아", "황도", "백도")),
        TOMATO("토마토", Set.of("토마토")),
        SULFITES("아황산류", Set.of("아황산", "보존료")),
        WALNUT("호두", Set.of("호두")),
        CHICKEN("닭고기", Set.of("닭", "닭고기", "닭가슴살", "닭다리", "닭안심")),
        BEEF("쇠고기", Set.of("쇠고기", "소고기", "우둔살", "등심" , "차돌박이", "안심")),
        SQUID("오징어", Set.of("오징어")),
        OYSTER("굴", Set.of("굴")),
        ABALONE("전복", Set.of("전복")),
        MUSSEL("홍합", Set.of("홍합")),
        PINE_NUT("잣", Set.of("잣")),
        EXTRACTED_INGREDIENTS("추출성분(젤라틴 등)", Set.of("젤라틴")),
        NONE("없음", Set.of());

        private final String koreanName;
        private final Set<String> keywords;

        Allergen(String koreanName, Set<String> keywords) {
                this.koreanName = koreanName;
                this.keywords = keywords;
        }

        public String getKoreanName() {
                return koreanName;
        }

        /** 재료 문자열에 이 알레르기가 포함되는지 검사 */
        public boolean matchesIngredient(String ingredientLine) {
                if (ingredientLine == null || ingredientLine.isBlank()) return false;

                String normalized = ingredientLine.replaceAll("\\s+", "");

                return keywords.stream()
                        .anyMatch(k ->
                                normalized.contains(k.replaceAll("\\s+", ""))
                        );
        }

        // ===== 기존 코드들 그대로 유지 =====
        private static final Map<String, Allergen> BY_ENUM_NAME =
                Arrays.stream(values())
                        .collect(Collectors.toUnmodifiableMap(
                                a -> normalizeUpper(a.name()),
                                a -> a
                        ));

        private static final Map<String, Allergen> BY_KOREAN =
                Arrays.stream(values())
                        .collect(Collectors.toUnmodifiableMap(
                                a -> normalize(a.koreanName),
                                a -> a
                        ));

        private static final Map<String, Allergen> ALIASES = Map.ofEntries(
                Map.entry(normalize("계란"), EGG),
                Map.entry(normalize("소고기"), BEEF),
                Map.entry(normalize("추출성분"), EXTRACTED_INGREDIENTS)
        );

        public static Allergen from(String name) {
                Allergen a = BY_ENUM_NAME.get(normalizeUpper(name));
                if (a == null) throw new IllegalArgumentException("Unknown allergen: " + name);
                return a;
        }

        public static Allergen fromKorean(String koreanName) {
                String key = normalize(koreanName);
                if (ALIASES.containsKey(key)) return ALIASES.get(key);

                Allergen a = BY_KOREAN.get(key);
                if (a == null) throw new IllegalArgumentException("Unknown allergen: " + koreanName);
                return a;
        }

        private static String normalize(String s) {
                return s == null ? "" : s.trim();
        }

        private static String normalizeUpper(String s) {
                return normalize(s).toUpperCase(Locale.ROOT);
        }
}




