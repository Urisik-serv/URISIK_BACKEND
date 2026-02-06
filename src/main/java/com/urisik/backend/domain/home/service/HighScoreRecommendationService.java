package com.urisik.backend.domain.home.service;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.home.candidate.*;
import com.urisik.backend.domain.home.converter.HighScoreRecommendationConverter;
import com.urisik.backend.domain.home.dto.HighScoreRecommendationDTO;
import com.urisik.backend.domain.home.dto.HighScoreRecommendationResponse;
import com.urisik.backend.domain.home.policy.CategoryMapper;
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
     * - 카테고리 미선택: 전체 레시피 + 변형 레시피 중 고평점 추천
     * - 카테고리 선택: 해당 카테고리 내에서 고평점 추천
     * - 알레르기는 필터링하지 않음
     * - 단, 평점 동점 시 알레르기 안전한 레시피 우선
     */
    public HighScoreRecommendationResponse recommend(
            Long loginUserId,
            String category
    ) {
        // 1️. 로그인 사용자 → 가족방 ID 조회
        Long familyRoomId =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND))
                        .getFamilyRoom()
                        .getId();

        // 2️. 후보 조회 (최대 20개씩)
        Pageable pageable = PageRequest.of(0, 20);
        List<HighScoreRecipeCandidate> candidates = new ArrayList<>();

        // 2-1) 카테고리 미선택
        if (category == null || category.isBlank()) {

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
        }
        // 2-2) 카테고리 선택
        else {
            List<String> legacyCategories =
                    CategoryMapper.toLegacyList(category);

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

        // 3️. 정렬
        candidates.sort(
                Comparator
                        // 1차: 평점 내림차순
                        .comparingDouble(HighScoreRecipeCandidate::getAvgScore)
                        .reversed()
                        // 2차: 평점 동점 시 알레르기 안전 우선
                        .thenComparing(candidate -> {
                            List<Allergen> risks =
                                    allergyRiskService.detectRiskAllergens(
                                            familyRoomId,
                                            candidate.getIngredients()
                                    );
                            return risks.isEmpty(); // true = 안전
                        }).reversed()
        );

        // 4️. Top 3 추출 + DTO 변환
        List<HighScoreRecommendationDTO> result =
                candidates.stream()
                        .limit(3)
                        .map(converter::toDto)
                        .toList();

        return new HighScoreRecommendationResponse(result);
    }
}

