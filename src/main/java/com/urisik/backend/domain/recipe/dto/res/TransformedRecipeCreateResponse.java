package com.urisik.backend.domain.recipe.dto.res;

import com.urisik.backend.domain.recipe.dto.RecipeStepDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TransformedRecipeCreateResponse {

    private Long transformedRecipeId;
    private String title;
    private String imageUrl;
    private Long baseRecipeId;
    private boolean validationStatus;

    private List<String> ingredients;
    private List<RecipeStepDTO> steps;
    private List<SubstitutionSummaryDTO> substitutionSummary;

    @Getter
    @AllArgsConstructor
    public static class SubstitutionSummaryDTO {
        private String allergen;
        private String replacedWith;
        private String reason;
    }
}
