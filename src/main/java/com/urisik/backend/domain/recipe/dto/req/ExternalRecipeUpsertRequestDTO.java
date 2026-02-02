package com.urisik.backend.domain.recipe.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExternalRecipeUpsertRequestDTO {

    /**
     * 외부 레시피 ID (RCP_SEQ)
     */
    @NotBlank
    private String rcpSeq;

    /**
     * 레시피 이름 (RCP_NM)
     */
    @NotBlank
    private String rcpNm;

    /**
     * 외부 API 원본 재료 문자열
     */
    @NotBlank
    private String ingredientsRaw;

    /**
     * 외부 API 원본 조리법 문자열
     */
    @NotBlank
    private String instructionsRaw;

    /**
     * 외부 메타데이터 (영양 / 이미지 / 카테고리)
     */
    @Valid
    @NotNull
    private Metadata metadata;

    @Getter
    @NoArgsConstructor
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
}

