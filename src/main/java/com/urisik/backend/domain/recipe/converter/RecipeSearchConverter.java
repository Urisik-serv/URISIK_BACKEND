package com.urisik.backend.domain.recipe.converter;

import com.urisik.backend.domain.recipe.dto.res.RecipeSearchResponseDTO;
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
                meta != null ? meta.getImageSmallUrl() : null,
                meta != null ? meta.getCategory() : null,
                recipe.getAvgScore(),
                recipe.getReviewCount(),
                recipe.getWishCount(),
                null

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
                tr.getBaseRecipe().getTitle(),
                meta != null ? meta.getImageSmallUrl() : null,
                meta != null ? meta.getCategory() : null,
                tr.getAvgScore(),
                tr.getReviewCount(),
                tr.getWishCount(),
                null
        );
    }

    /** 외부 API 레시피 */
    public static RecipeSearchResponseDTO.Item fromExternal(FoodSafetyRecipeResponse.Row row, String instructionsRaw) {
        RecipeSearchResponseDTO.ExternalSnapshot snapshot =
                new RecipeSearchResponseDTO.ExternalSnapshot(
                        row.getRcpSeq(),
                        row.getRcpNm(),
                        row.getCategory(),
                        row.getServingWeight(),
                        row.getCalorie(),
                        row.getCarbohydrate(),
                        row.getProtein(),
                        row.getFat(),
                        row.getSodium(),
                        row.getImageSmall(),
                        row.getImageLarge(),
                        row.getIngredientsRaw(),
                        instructionsRaw
                );

        return new RecipeSearchResponseDTO.Item(
                "EXT-" + row.getRcpSeq(),
                "EXTERNAL",
                row.getRcpNm(),
                row.getImageSmall(),
                row.getCategory(),
                null,
                null,
                null,
                snapshot
        );
    }
}

