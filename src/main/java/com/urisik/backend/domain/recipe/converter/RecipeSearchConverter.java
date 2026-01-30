package com.urisik.backend.domain.recipe.converter;

import com.urisik.backend.domain.recipe.dto.RecipeSearchResponseDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.dto.FoodSafetyRecipeResponse;

public class RecipeSearchConverter {

    private RecipeSearchConverter() {}

    /** 내부 원본 레시피 */
    public static RecipeSearchResponseDTO.Item fromRecipe(
            Recipe recipe,
            RecipeExternalMetadata meta
    ) {
        return new RecipeSearchResponseDTO.Item(
                recipe.getId().toString(),
                "RECIPE",
                recipe.getTitle(),
                meta != null ? meta.getCategory() : null,
                meta != null ? meta.getImageSmallUrl() : null,
                recipe.getAvgScore(),
                recipe.getReviewCount()
        );
    }

    /** 변형 레시피 */
    public static RecipeSearchResponseDTO.Item fromTransformed(
            TransformedRecipe tr,
            RecipeExternalMetadata meta
    ) {
        return new RecipeSearchResponseDTO.Item(
                tr.getId().toString(),
                "TRANSFORMED",
                tr.getRecipe().getTitle(),
                meta != null ? meta.getCategory() : null,
                meta != null ? meta.getImageSmallUrl() : null,
                tr.getAvgScore(),
                tr.getReviewCount()
        );
    }

    /** 외부 API 레시피 */
    public static RecipeSearchResponseDTO.Item fromExternal(
            FoodSafetyRecipeResponse.Row row
    ) {
        return new RecipeSearchResponseDTO.Item(
                "EXT-" + row.getRcpSeq(),
                "EXTERNAL",
                row.getRcpNm(),
                row.getCategory(),
                row.getImageSmall(),
                null,
                null
        );
    }
}

