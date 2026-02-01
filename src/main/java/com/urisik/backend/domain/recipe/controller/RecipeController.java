package com.urisik.backend.domain.recipe.controller;

import com.urisik.backend.domain.recipe.dto.RecipeDetailResponseDTO;
import com.urisik.backend.domain.recipe.enums.RecipeSuccessCode;
import com.urisik.backend.domain.recipe.service.RecipeReadService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
@Tag(name = "Recipes", description = "레시피 관련 API")
public class RecipeController {

    private final RecipeReadService recipeReadService;

    @GetMapping("/{recipeId}")
    @Operation(summary = "레시피 상세 조회 API", description = "외부 Recipe API 또는 AI로부터 수집된 레시피를 조회 api 입니다.")
    public ApiResponse<RecipeDetailResponseDTO> getRecipeDetail(@PathVariable Long recipeId) {
        return ApiResponse.onSuccess(
                RecipeSuccessCode.RECIPE_DETAIL_OK,
                recipeReadService.getRecipeDetail(recipeId)
        );
    }
}

