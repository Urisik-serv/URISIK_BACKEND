package com.urisik.backend.domain.recipe.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalRecipeUpsertRequestDTO {

    @NotBlank
    private String rcpSeq;

    @NotBlank
    private String rcpNm;

    @NotBlank
    private String ingredientsRaw;

    @NotBlank
    private String instructionsRaw;

    @Valid
    @NotNull
    private Metadata metadata;

    /* ================= Metadata ================= */

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {

        private String category;
        private String servingWeight;

        private Integer calorie;
        private Integer carbohydrate;
        private Integer protein;
        private Integer fat;
        private Integer sodium;

        private String imageSmallUrl;
        private String imageLargeUrl;
    }


    /* ================= Factory ================= */

    public static ExternalRecipeUpsertRequestDTO from(
            ExternalRecipeSnapshotDTO s
    ) {
        return new ExternalRecipeUpsertRequestDTO(
                s.getRcpSeq(),
                s.getRcpNm(),
                s.getIngredientsRaw(),
                s.getInstructionsRaw(),
                new Metadata(
                        trimToNull(s.getCategory()),
                        normalizeServingWeight(s.getServingWeight()),
                        safeInt(s.getCalorie()),
                        safeInt(s.getCarbohydrate()),
                        safeInt(s.getProtein()),
                        safeInt(s.getFat()),
                        safeInt(s.getSodium()),
                        trimToNull(s.getImageSmall()),
                        trimToNull(s.getImageLarge())
                )
        );
    }

    /* ================= 내부 유틸 ================= */

    private static Integer safeInt(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return (int) Double.parseDouble(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    private static String normalizeServingWeight(String w) {
        // FoodSafety API는 사실상 전부 1인분
        return "1인분";
    }
}

