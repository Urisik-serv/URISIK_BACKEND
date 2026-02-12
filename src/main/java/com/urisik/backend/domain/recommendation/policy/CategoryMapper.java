package com.urisik.backend.domain.recommendation.policy;

import java.util.List;

public final class CategoryMapper {

    private CategoryMapper() {}

    public static String map(String legacyCategory) {
        if (legacyCategory == null) return UnifiedCategory.ETC;

        return switch (legacyCategory) {
            case "밥" -> UnifiedCategory.BOWL;
            case "국", "국&찌개" -> UnifiedCategory.SOUP;
            case "반찬" -> UnifiedCategory.SIDE;
            case "후식" -> UnifiedCategory.DESSERT;
            case "일품", "기타" -> UnifiedCategory.ETC;
            default -> UnifiedCategory.ETC;
        };
    }

    /**
     * 통합 카테고리 → 레거시 카테고리 목록
     * (쿼리용)
     */
    public static List<String> toLegacyList(String unifiedCategory) {

        if (unifiedCategory == null || unifiedCategory.isBlank()) {
            return List.of();
        }

        return switch (unifiedCategory) {
            case UnifiedCategory.SOUP ->
                    List.of("국", "국&찌개");

            case UnifiedCategory.ETC ->
                    List.of("일품", "기타");

            default ->
                    List.of(unifiedCategory);
        };
    }
}

