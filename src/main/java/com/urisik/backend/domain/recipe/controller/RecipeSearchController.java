package com.urisik.backend.domain.recipe.controller;

import com.urisik.backend.domain.recipe.dto.res.RecipeSearchResponseDTO;
import com.urisik.backend.domain.recipe.enums.RecipeSuccessCode;
import com.urisik.backend.domain.recipe.service.RecipeSearchService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
@Tag(name = "Recipes", description = "레시피 관련 API")
public class RecipeSearchController {

    private final RecipeSearchService recipeSearchService;

    @GetMapping("/search")
    @Operation(summary = "레시피 검색 API", description = "검색어를 통해 여러 레시피들을 검색하는 api 입니다.")
    public ApiResponse<RecipeSearchResponseDTO> search(
            @AuthenticationPrincipal Long loginUserId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.onSuccess(
                RecipeSuccessCode.RECIPE_SEARCH_OK,
                recipeSearchService.search(loginUserId,keyword, page, size)
        );
    }
}

