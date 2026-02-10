package com.urisik.backend.domain.recipe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.recipe.dto.res.TransformedRecipeDetailResponseDTO;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransformedRecipeReadService {

    private final TransformedRecipeRepository transformedRecipeRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final AllergyRiskService allergyRiskService;
    private final ObjectMapper objectMapper;

    public TransformedRecipeDetailResponseDTO getTransformedRecipeDetail(
            Long transformedRecipeId,
            Long loginUserId
    ) {
        // 1. 변형 레시피 조회 (누구나 조회 가능)
        TransformedRecipe tr = transformedRecipeRepository.findById(transformedRecipeId)
                .orElseThrow(() ->
                        new GeneralException(RecipeErrorCode.TRANSFORMED_RECIPE_NOT_FOUND)
                );

        // 2. 로그인 사용자 → 가족방 조회 (알레르기 판별용)
        FamilyMemberProfile profile =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() ->
                                new GeneralException(GeneralErrorCode.NOT_FOUND)
                        );

        Long familyRoomId = profile.getFamilyRoom().getId();

        // 3. 재료 파싱
        List<String> ingredients =
                RecipeTextParser.parseIngredients(tr.getIngredientsRaw());

        // 4. 가족 기준 알레르기 위험 판별
        List<Allergen> risky =
                allergyRiskService.detectRiskAllergens(familyRoomId, ingredients);

        TransformedRecipeDetailResponseDTO.AllergyWarningDTO allergyWarning =
                risky.isEmpty()
                        ? new TransformedRecipeDetailResponseDTO.AllergyWarningDTO(
                        false,
                        List.of()
                )
                        : new TransformedRecipeDetailResponseDTO.AllergyWarningDTO(
                        true,
                        risky.stream()
                                .map(Allergen::getKoreanName)
                                .toList()
                );

        // 5. 대체 요약 파싱
        List<TransformedRecipeDetailResponseDTO.SubstitutionSummaryDTO> subs =
                parseSubstitutionSummary(tr.getSubstitutionSummaryJson());

        String category =
                Optional.ofNullable(tr.getBaseRecipe().getRecipeExternalMetadata())
                        .map(RecipeExternalMetadata::getCategory)
                        .orElse(null);

        // 6. DTO 조립
        return new TransformedRecipeDetailResponseDTO(
                tr.getId(),
                tr.getBaseRecipe().getTitle(),
                tr.getBaseRecipe().getId(),
                category,

                ingredients,
                RecipeTextParser.parseSteps(tr.getInstructionsRaw()),
                subs,
                allergyWarning,

                tr.getReviewCount(),
                tr.getAvgScore(),
                tr.getWishCount()
        );
    }

    /* ================== 내부 헬퍼 ================== */

    private List<TransformedRecipeDetailResponseDTO.SubstitutionSummaryDTO>
    parseSubstitutionSummary(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<TransformedRecipeDetailResponseDTO.SubstitutionSummaryDTO>>() {}
            );
        } catch (Exception e) {
            return List.of();
        }
    }
}

