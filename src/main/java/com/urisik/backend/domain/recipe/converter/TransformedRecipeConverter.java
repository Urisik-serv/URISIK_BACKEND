package com.urisik.backend.domain.recipe.converter;

import com.urisik.backend.domain.recipe.dto.res.TransformedRecipeResponseDTO;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;

import java.util.List;

public class TransformedRecipeConverter {

    private TransformedRecipeConverter() {}

    public static TransformedRecipeResponseDTO toDto(
            TransformedRecipe entity,
            List<String> ingredients,
            List<TransformedRecipeResponseDTO.StepDTO> steps,
            List<TransformedRecipeResponseDTO.SubstitutionSummaryDTO> summary
    ) {
        return new TransformedRecipeResponseDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getBaseRecipe().getId(),
                entity.getVisibility(),
                entity.isValidationStatus(),
                ingredients,
                steps,
                summary
        );
    }
}
