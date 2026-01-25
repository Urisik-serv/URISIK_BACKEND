package com.urisik.backend.domain.recipe.controller;

import com.urisik.backend.domain.recipe.enums.RecipeSuccessCode;
import com.urisik.backend.domain.recipe.dto.RecipeDetailDTO;
import com.urisik.backend.domain.recipe.dto.RecipeSearchResponseDTO;
import com.urisik.backend.domain.recipe.service.RecipeDetailService;
import com.urisik.backend.domain.recipe.service.RecipeSearchService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
@Tag(name = "Recipe", description = "레시피 관련 API")
public class RecipeController {

    private final RecipeSearchService recipeSearchService;
    private final RecipeDetailService recipeDetailService;

    /**
     * 음식명으로 레시피 검색 (검색 결과 없으면 404)
     */
    @GetMapping("/search")
    public ApiResponse<RecipeSearchResponseDTO> search(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @AuthenticationPrincipal(expression = "username") String userId
    ) {
        Long loginUserId = Long.parseLong(userId);

        RecipeSearchResponseDTO result =
                recipeSearchService.search(name, loginUserId, limit);

        return ApiResponse.onSuccess(RecipeSuccessCode.RECIPE_SEARCH_OK, result);
    }

    /**
     * recipeKey로 상세 조회
     */
    @GetMapping("/{recipeKey}")
    public ApiResponse<RecipeDetailDTO> detail(
            @PathVariable String recipeKey,
            @AuthenticationPrincipal(expression = "username") String userId
    ) {
        Long loginUserId = Long.parseLong(userId);

        RecipeDetailDTO result =
                recipeDetailService.getDetail(recipeKey, loginUserId);

        return ApiResponse.onSuccess(RecipeSuccessCode.RECIPE_DETAIL_OK, result);
    }

}
