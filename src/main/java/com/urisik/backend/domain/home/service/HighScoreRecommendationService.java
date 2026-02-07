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

    private final AllergyRiskService allergyRiskService;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;

    /**
     * 고평점 레시피 추천
     *
     * 정책 요약:
     * - 카테고리 미입력: 전체 추천
     * - 카테고리 입력:
     *   · 밥 / 국 / 반찬 / 후식 → 그대로 사용
     *   · 그 외 모든 값 → 기타로 자동 보정
     * - 알레르기 필터링 X
     * - 단, 평점 동점 시 가족 알레르기 기준 안전한 음식 우선
     */
    public HighScoreRecommendationResponse recommend(
            Long loginUserId,
            String category
    ) {
        /* 카테고리 정규화 (모르는 값 → 기타) */
        String normalizedCategory = normalizeCategory(category);

        /* 1️. 로그인 사용자 → 가족방 ID 조회 */
        Long familyRoomId =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND))
                        .getFamilyRoom()
                        .getId();

        /* 2. 추천 후보 조회 (각각 최대 20개) */
        Pageable pageable = PageRequest.of(0, 20);
        List<HighScoreRecipeCandidate> candidates = new ArrayList<>();

        //  후보 수집
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

        // 동점 처리
        candidates.sort(
                Comparator
                        .comparingDouble(HighScoreRecipeCandidate::getAvgScore)
                        .reversed()
                        .thenComparing(candidate -> {
                            List<Allergen> risks =
                                    allergyRiskService.detectRiskAllergens(
                                            familyRoomId,
                                            candidate.getIngredients()
                                    );
                            return risks.isEmpty();
                        })
                        .reversed()
        );

        return new HighScoreRecommendationResponse(
                candidates.stream()
                        .limit(3)
                        .map(converter::toDto)
                        .toList()
        );
    }

    /* 카테고리 정규화 메서드 */
    private String normalizeCategory(String category) {

        // 카테고리 미입력 → 전체 추천
        if (category == null || category.isBlank()) {
            return null;
        }

        // 서비스에서 명확히 구분하는 카테고리
        return switch (category) {
            case UnifiedCategory.BOWL,    // 밥
                 UnifiedCategory.SOUP,    // 국
                 UnifiedCategory.SIDE,    // 반찬
                 UnifiedCategory.DESSERT  // 후식
                    -> category;

            // 그 외 모든 값은 기타
            default
                    -> UnifiedCategory.ETC;
        };
    }
}


