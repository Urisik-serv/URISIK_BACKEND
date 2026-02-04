package com.urisik.backend.domain.recipe.controller;

import com.urisik.backend.domain.member.service.MemberService;
import com.urisik.backend.domain.recipe.dto.res.HomeSafeRecipeResponse;
import com.urisik.backend.domain.recipe.enums.RecipeSuccessCode;
import com.urisik.backend.domain.recipe.service.HomeRecommendationService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "Home", description = "홈 화면 관련 API")
public class HomeRecommendationController {

    private final HomeRecommendationService homeRecommendationService;

    @GetMapping("/recommendations/safe-recipes")
    @Operation(
            summary = "홈 안전 레시피 추천 API",
            description = "위시리스트가 많은 레시피 중 로그인 사용자의 가족 알레르기 기준으로 안전한 레시피 Top 3를 추천합니다."
    )
    public ApiResponse<HomeSafeRecipeResponse> getSafeRecipeRecommendations(
            @AuthenticationPrincipal Long loginUserId
    ) {
        HomeSafeRecipeResponse result =
                homeRecommendationService.recommendSafeRecipes(loginUserId);

        return ApiResponse.onSuccess(
                RecipeSuccessCode.HOME_SAFE_RECIPE_OK,
                result
        );
    }
}
