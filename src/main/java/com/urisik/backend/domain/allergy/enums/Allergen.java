package com.urisik.backend.domain.allergy.enums;


import java.util.Arrays;

import java.util.*;
import java.util.stream.Collectors;

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

        // ===== 캐시 맵: Enum name 기반 (EGG 등) =====
        private static final Map<String, Allergen> BY_ENUM_NAME =
                Arrays.stream(values())
                        .collect(Collectors.toUnmodifiableMap(
                                a -> normalizeUpper(a.name()),
                                a -> a
                        ));

        // ===== 캐시 맵: 한글명 기반 ("달걀" 등) =====
        private static final Map<String, Allergen> BY_KOREAN =
                Arrays.stream(values())
                        .collect(Collectors.toUnmodifiableMap(
                                a -> normalize(a.koreanName),
                                a -> a
                        ));

        // ===== Alias: 입력이 흔들릴 때 대응 (필요한 만큼 추가) =====
        private static final Map<String, Allergen> ALIASES = Map.ofEntries(
                Map.entry(normalize("계란"), EGG),              // "계란"도 달걀로 취급
                Map.entry(normalize("달걀 "), EGG),             // 공백
                Map.entry(normalize("소고기"), BEEF),           // "소고기" -> BEEF (koreanName은 쇠고기)
                Map.entry(normalize("추출성분"), EXTRACTED_INGREDIENTS) // 괄호 없는 버전
        );

        /** "EGG" 같은 enum name 문자열을 받는 경우 */
        public static Allergen from(String name) {
                if (name == null || name.isBlank()) {
                        throw new IllegalArgumentException("Allergen name is blank");
                }
                Allergen a = BY_ENUM_NAME.get(normalizeUpper(name));
                if (a == null) {
                        throw new IllegalArgumentException("Unknown allergen: " + name);
                }
                return a;
        }

        /** "달걀", "계란", "소고기" 같은 한글 문자열을 받는 경우 */
        public static Allergen fromKorean(String koreanName) {
                if (koreanName == null || koreanName.isBlank()) {
                        throw new IllegalArgumentException("Allergen koreanName is blank");
                }

                String key = normalize(koreanName);

                // 1) alias 우선
                Allergen alias = ALIASES.get(key);
                if (alias != null) return alias;

                // 2) 정식 한글명
                Allergen a = BY_KOREAN.get(key);
                if (a == null) {
                        throw new IllegalArgumentException("Unknown allergen (korean): " + koreanName);
                }
                return a;
        }

        private static String normalize(String s) {
                return s == null ? "" : s.trim();
        }

        private static String normalizeUpper(String s) {
                return normalize(s).toUpperCase(Locale.ROOT);
        }
}



