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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishHighScoreRecommendationService {

    private final HomeRepository homeRepository;
    private final HomeTransformedRecipeRepository homeTransformedRecipeRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final AllergyRiskService allergyRiskService;
    private final HighScoreRecommendationConverter converter;

    /**
     *  위시리스트 많은 순 추천 (Top 3 고정)
     *
     * 규칙:
     * 1) wishCount desc
     * 2) wish 같으면 avgScore desc
     * 3) avgScore 같으면 reviewCount desc
     * 4) 위 3개가 모두 같을 때만 알레르기 안전 우선
     * 5) 카테고리 선택 시 해당 카테고리 내에서만
     */
    public HighScoreRecommendationResponse recommend(
            Long loginUserId,
            String category
    ) {
        // 1️. 로그인 사용자 → 가족방
        FamilyMemberProfile profile =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));

        Long familyRoomId = profile.getFamilyRoom().getId();
        String normalizedCategory = normalizeCategory(category);

        Pageable pageable = PageRequest.of(0, 30); // 후보는 넉넉히
        List<HighScoreRecipeCandidate> candidates = new ArrayList<>();

        /* =========================
         * 2️. 후보 수집 (DB에서 위시 기준 정렬)
         * ========================= */
        if (normalizedCategory == null) {
            candidates.addAll(
                    homeRepository.findTopByWish(pageable)
                            .stream()
                            .map(RecipeCandidateLow::new)
                            .toList()
            );
            candidates.addAll(
                    homeTransformedRecipeRepository.findTopByWish(pageable)
                            .stream()
                            .map(TransformedRecipeCandidateLow::new)
                            .toList()
            );
        } else {
            List<String> legacyCategories =
                    CategoryMapper.toLegacyList(normalizedCategory);

            candidates.addAll(
                    homeRepository.findTopByWishAndCategories(legacyCategories, pageable)
                            .stream()
                            .map(RecipeCandidateLow::new)
                            .toList()
            );
            candidates.addAll(
                    homeTransformedRecipeRepository.findTopByWishAndCategories(legacyCategories, pageable)
                            .stream()
                            .map(TransformedRecipeCandidateLow::new)
                            .toList()
            );
        }

        /* =========================
         * 3️. 완전 동점자 알레르기 tie-break
         * ========================= */
        List<HighScoreRecipeCandidate> finalSorted =
                applyAllergyTieBreaker(candidates, familyRoomId);

        /* =========================
         * 4️. Top 3 반환
         * ========================= */
        return new HighScoreRecommendationResponse(
                finalSorted.stream()
                        .limit(3)
                        .map(converter::toDto)
                        .toList()
        );
    }

    /**
     * wish + avgScore + reviewCount가 모두 같은 "연속 구간"에서만
     * 알레르기 안전 우선
     */
    private List<HighScoreRecipeCandidate> applyAllergyTieBreaker(
            List<HighScoreRecipeCandidate> candidates,
            Long familyRoomId
    ) {
        if (candidates.size() <= 1) return candidates;

        List<HighScoreRecipeCandidate> result = new ArrayList<>();
        int i = 0;

        while (i < candidates.size()) {
            HighScoreRecipeCandidate base = candidates.get(i);
            List<HighScoreRecipeCandidate> group = new ArrayList<>();
            group.add(base);

            int j = i + 1;
            while (j < candidates.size()
                    && base.getWishCount() == candidates.get(j).getWishCount()
                    && base.getAvgScore() == candidates.get(j).getAvgScore()
                    && base.getReviewCount() == candidates.get(j).getReviewCount()
            ) {
                group.add(candidates.get(j));
                j++;
            }

            if (group.size() > 1) {
                group.sort(
                        (a, b) -> Boolean.compare(
                                isSafe(familyRoomId, b),
                                isSafe(familyRoomId, a)
                        )
                );
            }

            result.addAll(group);
            i = j;
        }

        return result;
    }

    private boolean isSafe(Long familyRoomId, HighScoreRecipeCandidate c) {
        return allergyRiskService
                .detectRiskAllergens(familyRoomId, c.getIngredients())
                .isEmpty();
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) return null;

        return switch (category) {
            case UnifiedCategory.BOWL,
                 UnifiedCategory.SOUP,
                 UnifiedCategory.SIDE,
                 UnifiedCategory.DESSERT -> category;
            default -> UnifiedCategory.ETC;
        };
    }
}

