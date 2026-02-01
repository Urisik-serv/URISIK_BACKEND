package com.urisik.backend.domain.recipe.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExternalRecipeUpsertRequestDTO {
    private String rcpSeq;
    private String rcpNm;            // rcpNm
    private String category;
    private String servingWeight;

    private String calorie;
    private String carbohydrate;
    private String protein;
    private String fat;
    private String sodium;

    private String imageSmallUrl;
    private String imageLargeUrl;

    private String ingredientsRaw;
    private String instructionsRaw;
}

