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
    private final ExternalRecipeConverter converter;

    @Transactional
    public ExternalRecipeUpsertResponseDTO upsertExternal(ExternalRecipeUpsertRequestDTO req) {

        Optional<Recipe> existing =
                recipeRepository.findBySourceRef(req.getRcpSeq());

        if (existing.isPresent()) {
            return new ExternalRecipeUpsertResponseDTO(
                    existing.get().getId(),
                    false
            );
        }

        Recipe recipe = recipeRepository.save(
                converter.toRecipe(req)
        );

        metadataRepository.save(
                converter.toMetadata(recipe, req)
        );

        return new ExternalRecipeUpsertResponseDTO(
                recipe.getId(),
                true
        );
    }
}


