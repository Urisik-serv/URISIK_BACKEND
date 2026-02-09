package com.urisik.backend.domain.recipe.infrastructure.external.ai.dto;

import com.urisik.backend.domain.recipe.dto.RecipeStepDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class GeminiTransformResult {

    private String title;
    private List<String> ingredients;
    private List<RecipeStepDTO> steps;
    private List<SubstitutionSummaryDTO> substitutionSummary;

    @Getter
    @NoArgsConstructor
    public static class SubstitutionSummaryDTO {
        private String allergen;
        private String replacedWith;
        private String reason;
    }
}

