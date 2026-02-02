package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.FoodSafetyRecipeClient;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.dto.FoodSafetyRecipeResponse;
import com.urisik.backend.domain.recipe.repository.RecipeExternalMetadataRepository;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalRecipeWriteService {

    private final RecipeRepository recipeRepository;
    private final RecipeExternalMetadataRepository metadataRepository;
    private final FoodSafetyRecipeClient foodSafetyRecipeClient;

    /**
     * 외부 레시피 메타데이터 보장 (없으면 생성)
     */
    @Transactional
    public void ensureExternalMetadata(String rcpSeq) {

        // 이미 recipe + meta가 있으면 아무 것도 안 함
        recipeRepository.findBySourceRef(rcpSeq)
                .flatMap(recipe ->
                        metadataRepository.findByRecipe_Id(recipe.getId())
                )
                .ifPresent(meta -> {
                    // 이미 있음 → 종료
                    return;
                });

        // 외부 API 조회
        FoodSafetyRecipeResponse.Row row =
                foodSafetyRecipeClient.fetchOneByRcpSeq(rcpSeq);

        if (row == null) {
            throw new GeneralException(
                    RecipeErrorCode.EXTERNAL_RECIPE_NOT_FOUND,
                    "외부 레시피를 찾을 수 없습니다. rcpSeq=" + rcpSeq
            );
        }

        // recipe는 반드시 이미 존재
        Recipe recipe = recipeRepository.findBySourceRef(rcpSeq)
                .orElseThrow(() ->
                        new GeneralException(
                                RecipeErrorCode.RECIPE_NOT_FOUND,
                                "recipe not found for rcpSeq=" + rcpSeq
                        ));

        RecipeExternalMetadata meta = new RecipeExternalMetadata(
                recipe,
                emptyIfNull(row.getCategory()),
                emptyIfNull(row.getServingWeight()),
                safeInt(row.getCalorie()),
                safeInt(row.getCarbohydrate()),
                safeInt(row.getProtein()),
                safeInt(row.getFat()),
                safeInt(row.getSodium()),
                emptyIfNull(row.getImageSmall()),
                emptyIfNull(row.getImageLarge())
        );

        metadataRepository.save(meta);
    }

    /* ===== helpers ===== */

    private Integer safeInt(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return (int) Double.parseDouble(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String emptyIfNull(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.isBlank() ? "" : t;
    }
}

