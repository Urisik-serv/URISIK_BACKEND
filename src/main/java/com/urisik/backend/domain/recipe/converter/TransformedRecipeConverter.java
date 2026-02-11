package com.urisik.backend.domain.recipe.converter;

import com.urisik.backend.domain.recipe.dto.res.TransformedRecipeCreateResponseDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.infrastructure.external.ai.dto.GeminiTransformResult;

public final class TransformedRecipeConverter {

    private TransformedRecipeConverter() {}

    public static TransformedRecipeCreateResponseDTO toCreateResponse(
            TransformedRecipe tr,
            Recipe recipe,
            GeminiTransformResult result
    ) {
        return new TransformedRecipeCreateResponseDTO(
                tr.getId(),
                tr.getTitle(),
                recipe.getId(),
                tr.isValidationStatus(),
                tr.getImageUrl(),
                result.getIngredients(),
                result.getSteps(),
                result.getSubstitutionSummary().stream()
                        .map(s -> new TransformedRecipeCreateResponseDTO.SubstitutionSummaryDTO(
                                s.getAllergen(),
                                s.getReplacedWith(),
                                s.getReason()
                        ))
                        .toList()
        );
    }
}
