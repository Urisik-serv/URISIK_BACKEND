package com.urisik.backend.domain.recipe.dto.res;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.recipe.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TransformedRecipeResponseDTO {

    private Long transformedRecipeId;
    private String title;
    private Long baseRecipeId;
    private Visibility visibility;
    private boolean validation_status;

    private List<String> ingredients;
    private List<StepDTO> steps;
    private List<SubstitutionSummaryDTO> substitutionSummary;

    @Getter
    @AllArgsConstructor
    public static class StepDTO {
        private int order;
        private String description;
    }

    @Getter
    @AllArgsConstructor
    public static class SubstitutionSummaryDTO {
        private Allergen allergen;
        private String replacedWith;
        private String reason;
    }
}
