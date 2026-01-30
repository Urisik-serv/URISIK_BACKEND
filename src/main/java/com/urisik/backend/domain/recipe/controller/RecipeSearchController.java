package com.urisik.backend.domain.recipe.controller;

import com.urisik.backend.domain.recipe.dto.RecipeSearchResponseDTO;
import com.urisik.backend.domain.recipe.enums.RecipeSuccessCode;
import com.urisik.backend.domain.recipe.service.RecipeSearchService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeSearchController {

    private final RecipeSearchService recipeSearchService;

    @GetMapping("/search")
    public ApiResponse<RecipeSearchResponseDTO> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.onSuccess(
                RecipeSuccessCode.RECIPE_SEARCH_OK,
                recipeSearchService.search(keyword, page, size)
        );
    }
}

