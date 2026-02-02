package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.recipe.dto.res.AllergyWarningDTO;
import com.urisik.backend.domain.recipe.dto.res.RecipeDetailResponseDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.enums.SourceType;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.FoodSafetyRecipeClient;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.dto.FoodSafetyRecipeResponse;
import com.urisik.backend.domain.recipe.repository.RecipeExternalMetadataRepository;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RecipeReadService {

    private final RecipeRepository recipeRepository;
    private final RecipeExternalMetadataRepository metadataRepository;

    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final AllergyRiskService allergyRiskService;

    private final ExternalRecipeWriteService externalRecipeWriteService;

    /**
     * 레시피 상세 조회
     * - 로그인 사용자 기준
     * - 가족 알레르기 판별
     * - 외부 레시피 메타데이터 자동 보완
     */
    @Transactional(readOnly = true)
    public RecipeDetailResponseDTO getRecipeDetail(
            Long recipeId,
            Long loginUserId
    ) {
        // 1️. 레시피 조회
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() ->
                        new GeneralException(RecipeErrorCode.RECIPE_NOT_FOUND));

        // 2️. 외부 레시피면 메타데이터 보장 (쓰기 트랜잭션 분리)
        if (recipe.getSourceType() == SourceType.EXTERNAL_API) {
            externalRecipeWriteService.ensureExternalMetadata(
                    recipe.getSourceRef()
            );
        }

        // 3️. 메타데이터 조회
        RecipeExternalMetadata meta =
                metadataRepository.findByRecipe_Id(recipe.getId())
                        .orElse(null);

        // 4️. 로그인 사용자 → 가족방
        FamilyMemberProfile profile =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() ->
                                new GeneralException(GeneralErrorCode.NOT_FOUND));

        Long familyRoomId = profile.getFamilyRoom().getId();

        // 5️. 재료 파싱
        List<String> ingredients =
                RecipeTextParser.parseIngredients(recipe.getIngredientsRaw());

        // 6️. 알레르기 판별
        List<Allergen> risky =
                allergyRiskService.detectRiskAllergens(familyRoomId, ingredients);

        RecipeDetailResponseDTO.AllergyWarningDTO warning =
                risky.isEmpty()
                        ? new RecipeDetailResponseDTO.AllergyWarningDTO(false, List.of())
                        : new RecipeDetailResponseDTO.AllergyWarningDTO(
                        true,
                        risky.stream()
                                .map(Allergen::getKoreanName)
                                .toList()
                );

        // 7️. DTO 반환
        return toDetailDto(recipe, meta, warning);
    }

    /* ================= DTO 변환 ================= */

    private RecipeDetailResponseDTO toDetailDto(
            Recipe recipe,
            RecipeExternalMetadata meta,
            RecipeDetailResponseDTO.AllergyWarningDTO warning
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
                recipe.getSourceType().name(),
                warning
        );
    }
}
