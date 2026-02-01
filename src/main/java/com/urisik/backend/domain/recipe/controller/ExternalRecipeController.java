package com.urisik.backend.domain.recipe.controller;

import com.urisik.backend.domain.recipe.dto.RecipeDetailResponseDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.enums.RecipeSuccessCode;
import com.urisik.backend.domain.recipe.service.RecipeReadService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
@Tag(name = "Recipes", description = "레시피 관련 API")
public class ExternalRecipeController {

    private final RecipeReadService recipeReadService;

    @GetMapping("/external/{rcpSeq}")
    @Operation(summary = "외부 레시피 상세 조회&내부 저장 API", description = "외부 레시피 상세 조회하고 내부에 저장하는 api 입니다.")
    public ApiResponse<RecipeDetailResponseDTO> getExternalDetail(
            @PathVariable String rcpSeq
    ) {
        Recipe recipe = recipeReadService.loadOrCreateByExternalId(rcpSeq);

        return ApiResponse.onSuccess(
                RecipeSuccessCode.RECIPE_DETAIL_OK,
                recipeReadService.getRecipeDetail(recipe.getId())
        );
    }
}
