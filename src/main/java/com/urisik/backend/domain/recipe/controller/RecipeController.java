package com.urisik.backend.domain.recipe.controller;

import com.urisik.backend.domain.recipe.dto.RecipeDetailResponseDTO;
import com.urisik.backend.domain.recipe.enums.RecipeSuccessCode;
import com.urisik.backend.domain.recipe.service.RecipeReadService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeReadService recipeReadService;

    @GetMapping("/{recipeId}")
    public ApiResponse<RecipeDetailResponseDTO> getRecipeDetail(@PathVariable Long recipeId) {
        return ApiResponse.onSuccess(
                RecipeSuccessCode.RECIPE_DETAIL_OK,
                recipeReadService.getRecipeDetail(recipeId)
        );
    }
}

