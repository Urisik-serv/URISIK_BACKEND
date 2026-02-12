package com.urisik.backend.domain.recipe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.recipe.dto.RecipeStepDTO;
import com.urisik.backend.domain.recipe.dto.RecipeStepDetailDTO;
import com.urisik.backend.domain.recipe.dto.res.TransformedRecipeDetailResponseDTO;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.entity.RecipeStep;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.entity.TransformedRecipeStepImage;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.repository.RecipeStepRepository;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeStepImageRepository;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransformedRecipeReadService {

    private final TransformedRecipeRepository transformedRecipeRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final AllergyRiskService allergyRiskService;
    private final TransformedRecipeStepImageRepository stepImageRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final ObjectMapper objectMapper;

    public TransformedRecipeDetailResponseDTO getTransformedRecipeDetail(
            Long transformedRecipeId,
            Long loginUserId
    ) {

        // 1. 변형 레시피 조회
        TransformedRecipe tr = transformedRecipeRepository.findById(transformedRecipeId)
                .orElseThrow(() ->
                        new GeneralException(RecipeErrorCode.TRANSFORMED_RECIPE_NOT_FOUND)
                );

        // 2. 로그인 사용자 가족방 조회
        FamilyMemberProfile profile =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() ->
                                new GeneralException(GeneralErrorCode.NOT_FOUND)
                        );

        Long familyRoomId = profile.getFamilyRoom().getId();

        // 3️. 재료 파싱
        List<String> ingredients =
                RecipeTextParser.parseIngredients(tr.getIngredientsRaw());

        // 4️. 알레르기 위험 판별
        List<Allergen> risky =
                allergyRiskService.detectRiskAllergens(familyRoomId, ingredients);

        TransformedRecipeDetailResponseDTO.AllergyWarningDTO allergyWarning =
                risky.isEmpty()
                        ? new TransformedRecipeDetailResponseDTO.AllergyWarningDTO(false, List.of())
                        : new TransformedRecipeDetailResponseDTO.AllergyWarningDTO(
                        true,
                        risky.stream()
                                .map(Allergen::getKoreanName)
                                .toList()
                );

        // 5️. 대체 요약 파싱
        List<TransformedRecipeDetailResponseDTO.SubstitutionSummaryDTO> subs =
                parseSubstitutionSummary(tr.getSubstitutionSummaryJson());

        // 6. 단계 파싱
        List<RecipeStepDTO> parsedSteps =
                RecipeTextParser.parseSteps(tr.getInstructionsRaw());

        // 7️. AI 생성 단계 이미지 조회
        List<TransformedRecipeStepImage> aiImages =
                stepImageRepository
                        .findByTransformedRecipeIdOrderByStepOrderAsc(tr.getId());

        Map<Integer, String> aiImageMap =
                aiImages.stream()
                        .collect(Collectors.toMap(
                                TransformedRecipeStepImage::getStepOrder,
                                TransformedRecipeStepImage::getImageUrl
                        ));

        // 8. 원본 레시피 단계 이미지 조회 (fallback용)
        List<RecipeStep> originalSteps =
                recipeStepRepository
                        .findByRecipe_IdOrderByStepOrderAsc(tr.getBaseRecipe().getId());

        Map<Integer, String> originalImageMap =
                originalSteps.stream()
                        .collect(Collectors.toMap(
                                RecipeStep::getStepOrder,
                                RecipeStep::getImageUrl
                        ));

        // 9️. 단계 DTO 조립 (AI → 원본 fallback)
        List<RecipeStepDetailDTO> finalSteps =
                parsedSteps.stream()
                        .map(step -> {

                            String imageUrl =
                                    aiImageMap.getOrDefault(
                                            step.getOrder(),
                                            originalImageMap.get(step.getOrder())
                                    );

                            return new RecipeStepDetailDTO(
                                    step.getOrder(),
                                    step.getDescription(),
                                    imageUrl
                            );
                        })
                        .toList();

        String category =
                Optional.ofNullable(tr.getBaseRecipe().getRecipeExternalMetadata())
                        .map(RecipeExternalMetadata::getCategory)
                        .orElse(null);

        // 10. 최종 DTO 반환
        return new TransformedRecipeDetailResponseDTO(
                tr.getId(),
                tr.getTitle(),
                tr.getBaseRecipe().getId(),
                category,
                tr.getImageUrl(),
                ingredients,
                finalSteps,
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

