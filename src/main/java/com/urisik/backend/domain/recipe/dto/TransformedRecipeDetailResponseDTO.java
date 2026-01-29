package com.urisik.backend.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TransformedRecipeDetailResponseDTO {

    private Long transformedRecipeId;
    private String title;
    private Long baseRecipeId;

    private List<String> ingredients;
    private List<RecipeStepDTO> steps;

    private List<SubstitutionSummaryDTO> substitutionSummary;

    private WarningDTO warning;
    private boolean createdByFamily;

    @Getter
    @AllArgsConstructor
    public static class SubstitutionSummaryDTO {
        private String allergen;
        private String replacedWith;
        private String reason;
    }

    @Getter
    @AllArgsConstructor
    public static class WarningDTO {
        private boolean hasRisk;
        private String message;
        private List<String> riskyAllergens;
    }

}

