package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.member.service.MemberService;
import com.urisik.backend.domain.recipe.converter.HomeSafeRecipeConverter;
import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.recipe.dto.res.HomeSafeRecipeDTO;
import com.urisik.backend.domain.recipe.dto.res.HomeSafeRecipeResponse;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeRecommendationService {

    private final RecipeRepository recipeRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final AllergyRiskService allergyRiskService;
    private final HomeSafeRecipeConverter homeSafeRecipeConverter;

    /**
     * 홈 안전 레시피 추천
     * - 로그인 사용자 기준
     * - 가족방 알레르기 필터
     * - 위시리스트 순 비교
     * - 안전한 레시피 3개 나오면 즉시 중단
     */
    @Transactional(readOnly = true)
    public HomeSafeRecipeResponse recommendSafeRecipes(Long loginUserId) {

        /* 1️. 로그인 사용자 → 가족방 조회 */
        FamilyMemberProfile profile =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() ->
                                new GeneralException(GeneralErrorCode.NOT_FOUND));

        Long familyRoomId = profile.getFamilyRoom().getId();

        /* 2️. 위시리스트 순 상위 후보 조회 (N+1 방지 쿼리) */
        List<Recipe> candidates =
                recipeRepository.findTopForHome(PageRequest.of(0, 20));

        List<HomeSafeRecipeDTO> result = new ArrayList<>();

        /* 3️. 하나씩 비교 → 안전한 것만 수집 */
        for (Recipe recipe : candidates) {

            // 재료 파싱
            List<String> ingredients =
                    RecipeTextParser.parseIngredients(recipe.getIngredientsRaw());

            // 가족 기준 알레르기 판별
            boolean hasRisk =
                    !allergyRiskService
                            .detectRiskAllergens(familyRoomId, ingredients)
                            .isEmpty();

            // 안전한 경우만 추가
            if (!hasRisk) {
                result.add(homeSafeRecipeConverter.toDto(recipe));
            }

            //  3개 채워지면 즉시 종료
            if (result.size() == 3) break;
        }

        return new HomeSafeRecipeResponse(result);
    }
}
