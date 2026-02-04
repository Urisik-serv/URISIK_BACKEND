package com.urisik.backend.domain.recipe.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecipeDetailResponseDTO {

    private Long recipeId;
    private String title;

    private String category;
    private String servingWeight;

    private NutritionDTO nutrition;
    private ImagesDTO images;

    private List<String> ingredients;
    private List<RecipeStepDTO> steps;

    private String sourceType; // EXTERNAL_API / AI_GENERATED

    private AllergyWarningDTO allergyWarning;

    private int reviewCount;

    private int wishCount;

    private double avgScore;

    @Getter
    @AllArgsConstructor
    public static class NutritionDTO {
        private Integer calorie;
        private Integer carbohydrate;
        private Integer protein;
        private Integer fat;
        private Integer sodium;
    }

    @Getter
    @AllArgsConstructor
    public static class ImagesDTO {
        private String small;
        private String large;
    }

    @Getter
    @AllArgsConstructor
    public static class AllergyWarningDTO {
        private boolean hasRisk;
        private List<String> allergens;
    }

}

