package com.urisik.backend.domain.home.controller;

import com.urisik.backend.domain.home.dto.HighScoreRecommendationResponse;
import com.urisik.backend.domain.home.dto.HomeSafeRecipeResponse;
import com.urisik.backend.domain.home.enums.HomeSuccessCode;
import com.urisik.backend.domain.home.service.HighScoreRecommendationService;
import com.urisik.backend.domain.home.service.SafeHighScoreRecommendationService;
import com.urisik.backend.domain.home.service.WishHighScoreRecommendationService;
import com.urisik.backend.domain.recipe.enums.RecipeSuccessCode;
import com.urisik.backend.domain.home.service.HomeRecommendationService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "Home", description = "홈 화면 관련 API")
public class HomeRecommendationController {

    private final HomeRecommendationService homeRecommendationService;

    @GetMapping("/recommendations/safe-recipes-top")
    @Operation(
            summary = "홈 안전 레시피 추천 API(상단)",
            description = "위시리스트가 많은 레시피 중 로그인 사용자의 가족 알레르기 기준으로 안전한 레시피 Top 3를 추천하는 api 입니다."
    )
    public ApiResponse<HomeSafeRecipeResponse> getSafeRecipeRecommendations(
            @AuthenticationPrincipal Long loginUserId
    ) {
        return ApiResponse.onSuccess(
                RecipeSuccessCode.HOME_SAFE_RECIPE_OK,
                homeRecommendationService.recommendSafeRecipes(loginUserId)
        );
    }

    private final HighScoreRecommendationService highScoreRecommendationService;

    @GetMapping("/high-score")
    @Operation(
            summary = "홈 평점 순 레시피 추천 API(하단)",
            description =  "카테고리 선택 여부에 따라 레시피 및 변형 레시피를 통합하여 별점이 높은 음식 Top 3를 추천하는 api 입니다."
    )
    public ApiResponse<HighScoreRecommendationResponse> recommendHighScore(
            @AuthenticationPrincipal Long loginUserId,
            @RequestParam(required = false) String category
    ) {
        HighScoreRecommendationResponse result =
                highScoreRecommendationService.recommend(loginUserId, category);

        return ApiResponse.onSuccess(
                HomeSuccessCode.RECOMMEND_HIGH_SCORE_OK,
                result
        );
    }

    private final SafeHighScoreRecommendationService safeHighScoreRecommendationService;

    @GetMapping("/safe-high-score")
    @Operation(
            summary = "홈 안전한 레시피 추천 API(하단)",
            description = "로그인한 사용자의 가족방 알레르기 기준으로 안전한 레시피만 필터링한 뒤, 평점이 높은 음식 Top 3를 추천하는 api 입니다."
    )
    public ApiResponse<HighScoreRecommendationResponse> recommendSafeHighScore(
            @AuthenticationPrincipal Long loginUserId,
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.onSuccess(
                HomeSuccessCode.RECOMMEND_SAFE_HIGH_SCORE_OK,
                safeHighScoreRecommendationService.recommendSafeRecipes(loginUserId, category)
        );
    }

    private WishHighScoreRecommendationService wishHighScoreRecommendationService;

    @GetMapping("/wish-high-score")
    @Operation(
            summary = "홈 위시리스트 많은 순 레시피 추천 API(하단)",
            description = "위시리스트 개수가 많은 순으로 정렬하되, 동일할 경우 평점·리뷰 수·알레르기 안전 여부를 기준으로 Top 3를 추천하는 api 입니다."
    )
    public ApiResponse<HighScoreRecommendationResponse> recommendWishHighScore(
            @AuthenticationPrincipal Long loginUserId,
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.onSuccess(
                HomeSuccessCode.RECOMMEND_WISH_HIGH_SCORE_OK,
                wishHighScoreRecommendationService.recommend(loginUserId, category)
        );
    }


}
