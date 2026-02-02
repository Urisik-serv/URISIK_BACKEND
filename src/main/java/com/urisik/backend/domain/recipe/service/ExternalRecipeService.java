package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.recipe.converter.ExternalRecipeConverter;
import com.urisik.backend.domain.recipe.dto.req.ExternalRecipeUpsertRequestDTO;
import com.urisik.backend.domain.recipe.dto.res.ExternalRecipeUpsertResponseDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.enums.SourceType;
import com.urisik.backend.domain.recipe.repository.RecipeExternalMetadataRepository;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExternalRecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeExternalMetadataRepository metadataRepository;

    @Transactional
    public ExternalRecipeUpsertResponseDTO upsertExternal(ExternalRecipeUpsertRequestDTO req) {

        // 1) 이미 저장된 외부 레시피면 그대로 반환
        Optional<Recipe> existing = recipeRepository.findBySourceRef(req.getRcpSeq());
        if (existing.isPresent()) {
            return new ExternalRecipeUpsertResponseDTO(existing.get().getId(), false);
        }

        // 2) 없으면 새로 저장
        Recipe recipe = new Recipe(
                required(req.getRcpNm(), "rcpNm"),
                required(req.getIngredientsRaw(), "ingredientsRaw"),
                required(req.getInstructionsRaw(), "instructionsRaw"),
                SourceType.EXTERNAL_API,
                required(req.getRcpSeq(), "rcpSeq")
        );
        Recipe saved = recipeRepository.save(recipe);

        RecipeExternalMetadata meta = new RecipeExternalMetadata(
                saved,
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
        metadataRepository.save(meta);

        return new ExternalRecipeUpsertResponseDTO(saved.getId(), true);
    }

    private String required(String s, String field) {
        if (s == null || s.trim().isBlank()) {
            throw new GeneralException(RecipeErrorCode.EXTERNAL_RECIPE_NOT_FOUND, field + " is blank");
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



