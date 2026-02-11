package com.urisik.backend.domain.recipe.converter;

import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.dto.res.RecipeDetailResponseDTO;
import com.urisik.backend.domain.recipe.dto.RecipeStepDetailDTO;

import java.util.List;

public final class RecipeDetailConverter {

    private RecipeDetailConverter() {}

    public static RecipeDetailResponseDTO toDetailDto(
            Recipe recipe,
            RecipeExternalMetadata meta,
            RecipeDetailResponseDTO.AllergyWarningDTO warning,
            List<String> ingredients,
            List<RecipeStepDetailDTO> stepDtos
    ) {
        return new RecipeDetailResponseDTO(
                recipe.getId(),
                recipe.getTitle(),
                meta != null ? meta.getCategory() : null,
                meta != null ? meta.getServingWeight() : null,
                new RecipeDetailResponseDTO.NutritionDTO(
                        meta != null ? meta.getCalorie() : null,
                        meta != null ? meta.getCarbohydrate() : null,
                        meta != null ? meta.getProtein() : null,
                        meta != null ? meta.getFat() : null,
                        meta != null ? meta.getSodium() : null
                ),
                new RecipeDetailResponseDTO.ImagesDTO(
                        meta != null ? meta.getImageSmallUrl() : null,
                        meta != null ? meta.getImageLargeUrl() : null
                ),
                ingredients,
                stepDtos,
                recipe.getSourceType().name(),
                warning,
                recipe.getReviewCount(),
                recipe.getWishCount(),
                recipe.getAvgScore()
        );
    }
}
