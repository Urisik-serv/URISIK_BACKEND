package com.urisik.backend.domain.home.service;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.home.candidate.*;
import com.urisik.backend.domain.home.converter.HighScoreRecommendationConverter;
import com.urisik.backend.domain.home.dto.HighScoreRecommendationDTO;
import com.urisik.backend.domain.home.dto.HighScoreRecommendationResponse;
import com.urisik.backend.domain.home.policy.CategoryMapper;
import com.urisik.backend.domain.home.policy.UnifiedCategory;
import com.urisik.backend.domain.home.repository.HomeRepository;
import com.urisik.backend.domain.home.repository.HomeTransformedRecipeRepository;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HighScoreRecommendationService {

    private final HomeRepository homeRepository;
    private final HomeTransformedRecipeRepository homeTransformedRecipeRepository;
    private final HighScoreRecommendationConverter converter;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;

    /**
     * 고평점 레시피 추천
     *
     * - 카테고리 미입력: 전체 추천
     * - 카테고리 입력:
     *   · 밥 / 국 / 반찬 / 후식 → 그대로
     *   · 그 외 → 기타
     * - 정렬은 DB에서만 수행
     */
    public HighScoreRecommendationResponse recommend(
            Long loginUserId,
            String category
    ) {
        String normalizedCategory = normalizeCategory(category);

        // 가족방 존재 여부만 확인 (정렬에는 사용 X)
        familyMemberProfileRepository.findByMember_Id(loginUserId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));

        Pageable pageable = PageRequest.of(0, 20);
        List<HighScoreRecipeCandidate> candidates = new ArrayList<>();

        // 1️. 후보 수집
        if (normalizedCategory == null) {

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

        //  Service에서는 정렬 X (DB 결과 그대로 사용)

        return new HighScoreRecommendationResponse(
                candidates.stream()
                        .limit(3)
                        .map(converter::toDto)
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
                 UnifiedCategory.DESSERT ->
                    category;
            default ->
                    UnifiedCategory.ETC;
        };
    }
}




