package com.urisik.backend.domain.recipe.controller;

import com.urisik.backend.domain.recipe.dto.req.ExternalRecipeUpsertRequestDTO;
import com.urisik.backend.domain.recipe.dto.res.ExternalRecipeUpsertResponseDTO;
import com.urisik.backend.domain.recipe.enums.RecipeSuccessCode;
import com.urisik.backend.domain.recipe.service.ExternalRecipeService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
@Tag(name = "Recipes", description = "레시피 관련 API")
public class ExternalRecipeController {

    private final ExternalRecipeService externalRecipeService;

    @PostMapping("/external")
    @Operation(summary = "외부 레시피 상세 저장 & 내부 레시피 생성 API", description = "외부 레시피를 내부 레시피로 저장하는 api 입니다.")
    public ApiResponse<ExternalRecipeUpsertResponseDTO> upsertExternal(
            @RequestBody ExternalRecipeUpsertRequestDTO request
    ) {
        return ApiResponse.onSuccess(
                RecipeSuccessCode.EXTERNAL_RECIPE_UPSERT_OK,
                externalRecipeService.upsertExternal(request)
        );
    }
}

