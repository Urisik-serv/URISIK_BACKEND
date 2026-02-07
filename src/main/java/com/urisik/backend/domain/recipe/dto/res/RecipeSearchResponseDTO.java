package com.urisik.backend.domain.recipe.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecipeSearchResponseDTO {

    private List<Item> items;

    @Getter
    @AllArgsConstructor
    public static class Item {
        private String id;          // EXTERNAL: "EXT-<RCP_SEQ>", RECIPE: "<recipeId>", TRANSFORMED: "<transformedRecipeId>"
        private String type;        // EXTERNAL / RECIPE / TRANSFORMED
        private String title;
        private String imageUrl;    // 음식 사진(대표)
        private String category;    // 요리종류(가능한 경우)
        private Double avgScore;
        private Integer reviewCount;
        private Integer wishCount;

        private String description;

        private ExternalSnapshot external;
    }

    @Getter
    @AllArgsConstructor
    public static class ExternalSnapshot {
        private String rcpSeq;
        private String rcpNm;
        private String category;
        private String servingWeight;

        private String calorie;
        private String carbohydrate;
        private String protein;
        private String fat;
        private String sodium;

        private String imageSmall;
        private String imageLarge;

        private String ingredientsRaw;
        private List<RecipeStepDetailDTO> steps;

    }
}

