package com.urisik.backend.domain.recipe.dto;

import com.urisik.backend.domain.allergy.dto.res.AllergySubstitutionResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecipeSummaryDTO {

    private String recipeKey;
    private String title;
    private String imageMain; // 검색: MAIN만
    private List<String> ingredients;

    private boolean hasAllergy;
    private List<AllergySubstitutionResponseDTO> substitutions;

}