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
    private final ExternalRecipeConverter externalRecipeConverter;

    @Transactional
    public ExternalRecipeUpsertResponseDTO upsertExternal(ExternalRecipeUpsertRequestDTO req) {

        // 1) 이미 저장된 외부 레시피면 그대로 반환
        Optional<Recipe> existing = recipeRepository.findBySourceRef(req.getRcpSeq());
        if (existing.isPresent()) {
            return new ExternalRecipeUpsertResponseDTO(existing.get().getId(), false);
        }

        // 2) 새로 저장
        Recipe recipe = externalRecipeConverter.toRecipe(req);
        Recipe saved = recipeRepository.save(recipe);

        RecipeExternalMetadata meta =
                externalRecipeConverter.toMetadata(saved, req);
        metadataRepository.save(meta);

        return new ExternalRecipeUpsertResponseDTO(saved.getId(), true);
    }

}




