package com.urisik.backend.domain.recipe.controller;

import com.urisik.backend.domain.recipe.dto.TransformedRecipeDetailResponseDTO;
import com.urisik.backend.domain.recipe.enums.RecipeSuccessCode;
import com.urisik.backend.domain.recipe.service.TransformedRecipeReadService;
import com.urisik.backend.global.apiPayload.ApiResponse;
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

    @GetMapping("/{transformedRecipeId}")
    public ApiResponse<TransformedRecipeDetailResponseDTO> getTransformedRecipeDetail(
            @PathVariable Long transformedRecipeId,
            @AuthenticationPrincipal(expression = "username") String userId
    ) {
        Long loginUserId = Long.parseLong(userId);

        return ApiResponse.onSuccess(
                RecipeSuccessCode.TRANSFORMED_RECIPE_DETAIL_OK,
                transformedRecipeReadService.getTransformedRecipeDetail(transformedRecipeId, loginUserId)
        );
    }
}
