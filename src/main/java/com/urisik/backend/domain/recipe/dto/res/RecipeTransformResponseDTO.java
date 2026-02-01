package com.urisik.backend.domain.recipe.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecipeTransformResponseDTO {

    private String type; // ORIGINAL / TRANSFORMED

    private Long recipeId;                 // ORIGINAL일 때
    private Long transformedRecipeId;      // TRANSFORMED일 때
    private Long baseRecipeId;

    private String title;
    private List<String> ingredients;
    private List<RecipeStepDTO> steps;

    private List<TransformedRecipeDetailResponseDTO.SubstitutionSummaryDTO> substitutionSummary;

}

