package com.urisik.backend.domain.recipe.infrastructure.ai;

import com.urisik.backend.domain.recipe.dto.TransformedRecipeDetailResponseDTO;

import java.util.List;

public interface RecipeAiClient {
    AiRewriteResult rewrite(
            String title,
            List<String> ingredients,
            List<TransformedRecipeDetailResponseDTO.SubstitutionSummaryDTO> rules
    );
}
