package com.urisik.backend.domain.home.service;

import com.urisik.backend.domain.home.candidate.HighScoreRecipeCandidate;
import com.urisik.backend.domain.home.candidate.RecipeCandidateLow;
import com.urisik.backend.domain.home.candidate.TransformedRecipeCandidateLow;
import com.urisik.backend.domain.home.converter.HighScoreRecommendationConverter;
import com.urisik.backend.domain.home.dto.HighScoreRecommendationResponse;
import com.urisik.backend.domain.home.policy.CategoryMapper;
import com.urisik.backend.domain.home.policy.UnifiedCategory;
import com.urisik.backend.domain.home.repository.HomeRepository;
import com.urisik.backend.domain.home.repository.HomeTransformedRecipeRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.recipe.service.AllergyRiskService;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafeHighScoreRecommendationService {

    private final HomeRepository homeRepository;
    private final HomeTransformedRecipeRepository homeTransformedRecipeRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final AllergyRiskService allergyRiskService;
    private final HighScoreRecommendationConverter converter;

    /**
     * 알레르기 안전 + 카테고리 기준 레시피 추천
     */
    public HighScoreRecommendationResponse recommendSafeRecipes(
            Long loginUserId,
            String category
    ) {
        // 1️. 로그인 사용자 → 가족방
        FamilyMemberProfile profile =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));

        Long familyRoomId = profile.getFamilyRoom().getId();
        String normalizedCategory = normalizeCategory(category);

        Pageable pageable = PageRequest.of(0, 50);
        List<HighScoreRecipeCandidate> candidates = new ArrayList<>();

        /* =========================
         * 2️. 카테고리 기준 후보 수집
         * ========================= */
        if (normalizedCategory == null) {
            // 카테고리 미선택 → 전체
            candidates.addAll(
                    homeRepository.findTopByScore(pageable)
                            .stream()
                            .map(RecipeCandidateLow::new)
                            .toList()
            );

            candidates.addAll(
                    homeTransformedRecipeRepository.findTopByScore(pageable)
                            .stream()
                            .map(TransformedRecipeCandidateLow::new)
                            .toList()
            );
        } else {
            List<String> legacyCategories =
                    CategoryMapper.toLegacyList(normalizedCategory);

            candidates.addAll(
                    homeRepository.findTopByCategories(legacyCategories, pageable)
                            .stream()
                            .map(RecipeCandidateLow::new)
                            .toList()
            );

            candidates.addAll(
                    homeTransformedRecipeRepository.findTopByCategories(legacyCategories, pageable)
                            .stream()
                            .map(TransformedRecipeCandidateLow::new)
                            .toList()
            );
        }

        /* =========================
         * 3. 알레르기 안전 필터 (핵심 차이)
         * ========================= */
        List<HighScoreRecipeCandidate> safeCandidates =
                candidates.stream()
                        .filter(c ->
                                allergyRiskService
                                        .detectRiskAllergens(
                                                familyRoomId,
                                                c.getIngredients()
                                        )
                                        .isEmpty()
                        )
                        .toList();

        /* =========================
         * 4️. 정렬 기준 유지
         * ========================= */
        safeCandidates.sort(
                Comparator.comparingDouble(HighScoreRecipeCandidate::getAvgScore).reversed()
                        .thenComparingInt(HighScoreRecipeCandidate::getReviewCount).reversed()
                        .thenComparingInt(HighScoreRecipeCandidate::getWishCount).reversed()
        );

        /* =========================
         * 5️. 결과 반환
         * ========================= */
        return new HighScoreRecommendationResponse(
                safeCandidates.stream()
                        .limit(3)
                        .map(c -> {
                    boolean isSafe =
                            allergyRiskService
                                    .detectRiskAllergens(
                                            familyRoomId,
                                            c.getIngredients()
                                    )
                                    .isEmpty();
                    return converter.toDto(c, isSafe);
                })
                        .toList()
        );
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }

        return switch (category) {
            case UnifiedCategory.BOWL,
                 UnifiedCategory.SOUP,
                 UnifiedCategory.SIDE,
                 UnifiedCategory.DESSERT -> category;
            default -> UnifiedCategory.ETC;
        };
    }
}
