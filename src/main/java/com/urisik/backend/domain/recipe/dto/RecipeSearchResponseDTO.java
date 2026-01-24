package com.urisik.backend.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecipeSearchResponseDTO {

    private String source; // INTERNAL_DB / EXTERNAL_API
    private List<RecipeSummaryDTO> recipes;

}