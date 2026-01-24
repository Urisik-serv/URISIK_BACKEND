package com.urisik.backend.domain.recipe.dto;

import com.urisik.backend.domain.allergy.dto.res.AllergySubstitutionResponseDTO;
import com.urisik.backend.domain.recipe.model.Nutrition;
import com.urisik.backend.domain.recipe.model.RecipeStep;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecipeDetailDTO {

    private String recipeKey;
    private String title;
    private String cookingMethod;
    private String category;

    private Nutrition nutrition;
    private List<String> hashtags;

    private String imageMain;  // MAIN
    private String imageLarge; // MK (상세 전용)

    private List<String> ingredients;
    private List<RecipeStep> steps;

    private String tip;

    private boolean hasAllergy;
    private List<AllergySubstitutionResponseDTO> substitutions;

}