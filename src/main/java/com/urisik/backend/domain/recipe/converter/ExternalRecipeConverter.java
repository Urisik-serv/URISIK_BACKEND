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

    public RecipeExternalMetadata toMetadata(Recipe recipe, ExternalRecipeUpsertRequestDTO req) {
        return new RecipeExternalMetadata(
                recipe,
                trimToNull(req.getCategory()),
                trimToNull(req.getServingWeight()),
                safeInt(req.getCalorie()),
                safeInt(req.getCarbohydrate()),
                safeInt(req.getProtein()),
                safeInt(req.getFat()),
                safeInt(req.getSodium()),
                trimToNull(req.getImageSmallUrl()),
                trimToNull(req.getImageLargeUrl())
        );
    }

    public RecipeExternalMetadata toMetadata(
            Recipe recipe,
            FoodSafetyRecipeResponse.Row row
    ) {
        return new RecipeExternalMetadata(
                recipe,
                trimToNull(row.getCategory()),        // RCP_PAT2
                trimToNull(row.getServingWeight()),   // INFO_WGT
                safeInt(row.getCalorie()),             // INFO_ENG
                safeInt(row.getCarbohydrate()),        // INFO_CAR
                safeInt(row.getProtein()),             // INFO_PRO
                safeInt(row.getFat()),                  // INFO_FAT
                safeInt(row.getSodium()),               // INFO_NA
                trimToNull(row.getImageSmall()),        // ATT_FILE_NO_MK
                trimToNull(row.getImageLarge())         // ATT_FILE_NO_MAIN
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

    private Integer safeInt(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return (int) Double.parseDouble(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}

