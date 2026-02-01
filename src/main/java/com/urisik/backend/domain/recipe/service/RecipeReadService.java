package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.recipe.dto.res.RecipeDetailResponseDTO;
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

    /**
     * 내부 레시피 상세 조회 (이미 DB에 있는 recipe)
     */
    @Transactional(readOnly = true)
    public RecipeDetailResponseDTO getRecipeDetail(Long recipeId) {

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new GeneralException(RecipeErrorCode.RECIPE_NOT_FOUND));

        RecipeExternalMetadata meta =
                metadataRepository.findByRecipe_Id(recipe.getId())
                        .orElse(null);

        return toDetailDto(recipe, meta);
    }

    /**
     * 외부 API 레시피 상세 조회 + 내부 저장
     */
    @Transactional
    public Recipe loadOrCreateByExternalId(String rcpSeq) {

        return recipeRepository.findBySourceRef(rcpSeq)
                .orElseGet(() -> {

                    FoodSafetyRecipeResponse.Row row =
                            foodSafetyRecipeClient.fetchOneByRcpSeq(rcpSeq);

                    if (row == null) {
                        throw new GeneralException(
                                RecipeErrorCode.EXTERNAL_RECIPE_NOT_FOUND,
                                "외부 레시피를 찾을 수 없습니다. rcpSeq=" + rcpSeq
                        );
                    }

                    String title = requiredText(row.getRcpNm(), "RCP_NM");
                    String ingredientsRaw =
                            requiredText(row.getIngredientsRaw(), "RCP_PARTS_DTLS");
                    String instructionsRaw =
                            requiredText(joinManuals(row), "MANUAL01~20");

                    Recipe recipe = new Recipe(
                            title,
                            ingredientsRaw,
                            instructionsRaw,
                            SourceType.EXTERNAL_API,
                            nullableText(row.getRcpSeq())
                    );

                    Recipe saved = recipeRepository.save(recipe);

                    RecipeExternalMetadata meta = new RecipeExternalMetadata(
                            saved,
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
                    return saved;
                });
    }

    /* ================= 내부 헬퍼 메서드 ================= */

    private RecipeDetailResponseDTO toDetailDto(
            Recipe recipe,
            RecipeExternalMetadata meta
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

    private String requiredText(String s, String fieldName) {
        if (s == null) {
            throw new GeneralException(
                    RecipeErrorCode.EXTERNAL_RECIPE_NOT_FOUND,
                    fieldName + " is null"
            );
        }
        String t = s.trim();
        if (t.isBlank()) {
            throw new GeneralException(
                    RecipeErrorCode.EXTERNAL_RECIPE_NOT_FOUND,
                    fieldName + " is blank"
            );
        }
        return t;
    }

    private String nullableText(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    private String emptyIfNull(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.isBlank() ? "" : t;
    }
}
