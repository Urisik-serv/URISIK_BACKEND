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
    public Recipe loadOrCreateByExternalId(String rcpSeq) {
        return recipeRepository.findBySourceRef(rcpSeq)
                .orElseGet(() -> {
                    FoodSafetyRecipeResponse.Row row = foodSafetyRecipeClient.fetchOneByRcpSeq(rcpSeq);
                    if (row == null) throw new GeneralException(RecipeErrorCode.EXTERNAL_RECIPE_NOT_FOUND);

                    String title = requiredText(row.getRcpNm(), "RCP_NM");
                    String ingredientsRaw = requiredText(row.getIngredientsRaw(), "RCP_PARTS_DTLS");
                    String instructionsRaw = requiredText(joinManuals(row), "MANUAL01~20"); // 합친 결과가 비면 예외

                    Recipe recipe = new Recipe(
                            title,
                            ingredientsRaw,
                            instructionsRaw,
                            SourceType.EXTERNAL_API,
                            nullableText(row.getRcpSeq())
                    );

                    Recipe saved = recipeRepository.save(recipe);

                    // metadata는 "빈 값이면 빈 문자열"로 저장 (DB NOT NULL이어도 안전)
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
                .orElse(""); // ✅ 비면 requiredText에서 잡아줄 거임
    }

    private Integer safeInt(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return (int) Double.parseDouble(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /** null/blank면 예외 (recipe 저장에 필요한 필수값) */
    private String requiredText(String s, String fieldName) {
        if (s == null) throw new GeneralException(RecipeErrorCode.EXTERNAL_RECIPE_NOT_FOUND, fieldName + " is null");
        String t = s.trim();
        if (t.isBlank()) throw new GeneralException(RecipeErrorCode.EXTERNAL_RECIPE_NOT_FOUND, fieldName + " is blank");
        return t;
    }

    /** null/blank면 null */
    private String nullableText(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    /** null/blank면 "" (DB NOT NULL 컬럼을 안전하게 채우기 용도) */
    private String emptyIfNull(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.isBlank() ? "" : t;
    }
}

