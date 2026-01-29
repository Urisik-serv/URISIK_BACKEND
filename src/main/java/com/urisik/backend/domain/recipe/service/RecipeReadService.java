package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.recipe.dto.RecipeDetailResponseDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.enums.SourceType;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.FoodSafetyRecipeClient;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.dto.FoodSafetyRecipeResponse;
import com.urisik.backend.domain.recipe.repository.RecipeExternalMetadataRepository;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RecipeReadService {

    private final RecipeRepository recipeRepository;
    private final RecipeExternalMetadataRepository metadataRepository;
    private final FoodSafetyRecipeClient foodSafetyRecipeClient;

    @Transactional
    public RecipeDetailResponseDTO getRecipeDetail(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new GeneralException(RecipeErrorCode.RECIPE_NOT_FOUND));

        RecipeExternalMetadata metadata =
                metadataRepository.findByRecipe_Id(recipe.getId())
                        .orElse(null);

        return toDetailDto(recipe, metadata);
    }

    // 외부 API row를 받아 DB에 저장하는 helper (recipeId 대신 sourceRef로 쓸 때 유용)
    @Transactional
    public Recipe loadOrCreateByExternalId(String rcpSeq) {
        return recipeRepository.findBySourceRef(rcpSeq)
                .orElseGet(() -> {
                    FoodSafetyRecipeResponse.Row row = foodSafetyRecipeClient.fetchOneByRcpSeq(rcpSeq);
                    if (row == null) throw new GeneralException(RecipeErrorCode.EXTERNAL_RECIPE_NOT_FOUND);

                    String instructionsRaw = joinManuals(row);

                    Recipe recipe = new Recipe(
                            row.getRcpNm(),
                            row.getIngredientsRaw(),
                            instructionsRaw,
                            SourceType.EXTERNAL_API,
                            row.getRcpSeq()
                    );
                    Recipe saved = recipeRepository.save(recipe);

                    RecipeExternalMetadata meta = new RecipeExternalMetadata(
                            saved,
                            row.getCategory(),
                            row.getServingWeight(),
                            safeInt(row.getCalorie()),
                            safeInt(row.getCarbohydrate()),
                            safeInt(row.getProtein()),
                            safeInt(row.getFat()),
                            safeInt(row.getSodium()),
                            row.getImageSmall(),
                            row.getImageLarge()
                    );
                    metadataRepository.save(meta);
                    return saved;
                });
    }

    private RecipeDetailResponseDTO toDetailDto(Recipe recipe, RecipeExternalMetadata meta) {
        return new RecipeDetailResponseDTO(
                recipe.getId(),
                recipe.getTitle(),
                meta == null ? null : meta.getCategory(),
                meta == null ? null : meta.getServingWeight(),
                new RecipeDetailResponseDTO.NutritionDTO(
                        meta == null ? null : meta.getCalorie(),
                        meta == null ? null : meta.getCarbohydrate(),
                        meta == null ? null : meta.getProtein(),
                        meta == null ? null : meta.getFat(),
                        meta == null ? null : meta.getSodium()
                ),
                new RecipeDetailResponseDTO.ImagesDTO(
                        meta == null ? null : meta.getImageSmallUrl(),
                        meta == null ? null : meta.getImageLargeUrl()
                ),
                RecipeTextParser.parseIngredients(recipe.getIngredientsRaw()),
                RecipeTextParser.parseSteps(recipe.getInstructionsRaw()),
                recipe.getSourceType().name()
        );
    }

    private String joinManuals(FoodSafetyRecipeResponse.Row row) {
        return Stream.of(
                        row.getManual01(), row.getManual02(), row.getManual03(), row.getManual04(), row.getManual05(),
                        row.getManual06(), row.getManual07(), row.getManual08(), row.getManual09(), row.getManual10(),
                        row.getManual11(), row.getManual12(), row.getManual13(), row.getManual14(), row.getManual15(),
                        row.getManual16(), row.getManual17(), row.getManual18(), row.getManual19(), row.getManual20()
                )
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
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

