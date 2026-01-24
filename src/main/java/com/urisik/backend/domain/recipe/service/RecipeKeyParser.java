package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;

public class RecipeKeyParser {

    public record Parsed(String source, String id) {}

    public static Parsed parse(String recipeKey) {
        if (recipeKey == null || !recipeKey.contains("-")) {
            throw new GeneralException(RecipeErrorCode.RECIPE_INVALID_KEY);
        }
        String[] parts = recipeKey.split("-", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new GeneralException(RecipeErrorCode.RECIPE_INVALID_KEY);
        }
        return new Parsed(parts[0], parts[1]);
    }

}
