package com.urisik.backend.domain.recipe.infrastructure.ai;

import com.urisik.backend.domain.recipe.dto.res.TransformedRecipeResponseDTO;

import java.util.ArrayList;
import java.util.List;

public class RecipeStepNormalizer {

    private RecipeStepNormalizer() {}

    public static List<TransformedRecipeResponseDTO.StepDTO> normalize(
            List<AiTransformedRecipePayload.Step> steps
    ) {
        if (steps == null) return List.of();

        List<TransformedRecipeResponseDTO.StepDTO> result = new ArrayList<>();
        int index = 1;

        for (AiTransformedRecipePayload.Step step : steps) {
            if (step == null || step.getDescription() == null) continue;

            int order = step.getOrder() != null ? step.getOrder() : index;
            result.add(
                    new TransformedRecipeResponseDTO.StepDTO(
                            order,
                            step.getDescription().trim()
                    )
            );
            index++;
        }

        return result;
    }
}
