package com.urisik.backend.domain.recipe.controller;

import com.urisik.backend.domain.recipe.dto.req.TransformRecipeRequestDTO;
import com.urisik.backend.domain.recipe.dto.res.TransformedRecipeResponseDTO;
import com.urisik.backend.domain.recipe.enums.RecipeSuccessCode;
import com.urisik.backend.domain.recipe.service.RecipeTransformService;
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
public class RecipeTransformController {

    private final RecipeTransformService recipeTransformService;

    @PostMapping("/{recipeId}/transform")
    @Operation(summary = "레시피 변형 생성 API", description = "가족들의 알레르기를 판별해 안전한 레시피를 생성하는 api 입니다.")
    public ApiResponse<TransformedRecipeResponseDTO> transform(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal Long loginUserId,
            @RequestBody TransformRecipeRequestDTO request
    ) {
        return ApiResponse.onSuccess(
                RecipeSuccessCode.RECIPE_TRANSFORM_CREATED,
                recipeTransformService.transform(recipeId, loginUserId, request)
        );
    }
}
