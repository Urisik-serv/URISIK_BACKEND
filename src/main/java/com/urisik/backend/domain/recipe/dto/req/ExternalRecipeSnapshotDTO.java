package com.urisik.backend.domain.recipe.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ExternalRecipeSnapshotDTO {

    private String rcpSeq;
    private String rcpNm;

    private String category;
    private String servingWeight; // 항상 "" (1인분 고정)

    private String calorie;
    private String carbohydrate;
    private String protein;
    private String fat;
    private String sodium;

    private String imageSmall;
    private String imageLarge;

    private String ingredientsRaw;
    private String instructionsRaw;

    private List<Step> steps;

    @Getter
    @AllArgsConstructor
    public static class Step {
        private int order;
        private String description;
        private String imageUrl;
    }
}