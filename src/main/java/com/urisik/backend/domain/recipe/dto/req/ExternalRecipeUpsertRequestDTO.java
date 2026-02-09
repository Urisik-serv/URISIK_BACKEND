package com.urisik.backend.domain.recipe.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalRecipeUpsertRequestDTO {

    private String rcpSeq;
    private String rcpNm;
    private String ingredientsRaw;
    private String instructionsRaw;
    private Metadata metadata;
    private List<Step> steps;

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

    /* ================= Step ================= */

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Step {
        private int order;
        private String description;
        private String imageUrl;
    }

}

