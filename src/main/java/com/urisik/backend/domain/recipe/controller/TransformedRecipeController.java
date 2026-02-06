package com.urisik.backend.domain.recipe.controller;

import com.urisik.backend.domain.recipe.dto.res.TransformedRecipeCreateResponse;
import com.urisik.backend.domain.recipe.dto.res.TransformedRecipeDetailResponseDTO;
import com.urisik.backend.domain.recipe.enums.RecipeSuccessCode;
import com.urisik.backend.domain.recipe.service.TransformedRecipeCreateService;
import com.urisik.backend.domain.recipe.service.TransformedRecipeReadService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transformed-recipes")
@Tag(name = "Recipes", description = "레시피 관련 API")
public class TransformedRecipeController {

    private final TransformedRecipeReadService transformedRecipeReadService;
    private final TransformedRecipeCreateService service;

    @PostMapping("/{recipeId}/transform")
    @Operation(summary = "변형 레시피 생성 API", description = "사용자 가족에 맞게 레시피를 변형 생성하는 api 입니다.")
    public ApiResponse<TransformedRecipeCreateResponse> transform(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        return ApiResponse.onSuccess(
                RecipeSuccessCode.RECIPE_TRANSFORM_CREATED,
                service.create(recipeId, loginUserId)
        );
    }

    @GetMapping("/{transformedRecipeId}")
    @Operation(
            summary = "변형 레시피 상세 조회 API",
            description = "사용자들이 생성한 변형 레시피의 상세 내용을 조회하는 api 입니다."
    )
    public ApiResponse<TransformedRecipeDetailResponseDTO> getDetail(
            @PathVariable Long transformedRecipeId,
            @AuthenticationPrincipal Long loginUserId   // ⭐ 여기
    ) {
        TransformedRecipeDetailResponseDTO result =
                transformedRecipeReadService.getTransformedRecipeDetail(
                        transformedRecipeId,
                        loginUserId
                );

        return ApiResponse.onSuccess(
                RecipeSuccessCode.TRANSFORMED_RECIPE_DETAIL_OK,
                result
        );
    }

}
