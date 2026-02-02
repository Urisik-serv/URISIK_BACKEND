package com.urisik.backend.domain.recipe.converter;

import com.urisik.backend.domain.recipe.dto.req.ExternalRecipeUpsertRequestDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.enums.SourceType;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.dto.FoodSafetyRecipeResponse;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Component;

@Component
public class ExternalRecipeConverter {

    public Recipe toRecipe(ExternalRecipeUpsertRequestDTO req) {
        return new Recipe(
                required(req.getRcpNm(), "rcpNm"),
                required(req.getIngredientsRaw(), "ingredientsRaw"),
                required(req.getInstructionsRaw(), "instructionsRaw"),
                SourceType.EXTERNAL_API,
                required(req.getRcpSeq(), "rcpSeq")
        );
    }

    public RecipeExternalMetadata toMetadata(
            Recipe recipe,
            ExternalRecipeUpsertRequestDTO req
    ) {
        ExternalRecipeUpsertRequestDTO.Metadata m = req.getMetadata();

        return new RecipeExternalMetadata(
                recipe,
                trimToNull(m.getCategory()),
                trimToNull(m.getServingWeight()),
                m.getCalorie(),
                m.getCarbohydrate(),
                m.getProtein(),
                m.getFat(),
                m.getSodium(),
                trimToNull(m.getImageSmallUrl()),
                trimToNull(m.getImageLargeUrl())
        );
    }

    /* ===== 내부 유틸 ===== */

    private String required(String s, String field) {
        if (s == null || s.isBlank()) {
            throw new GeneralException(
                    RecipeErrorCode.EXTERNAL_RECIPE_NOT_FOUND,
                    field + " is blank"
            );
        }
        return s.trim();
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

}

