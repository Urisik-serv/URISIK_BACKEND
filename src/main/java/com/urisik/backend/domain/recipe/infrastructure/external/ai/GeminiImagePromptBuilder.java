package com.urisik.backend.domain.recipe.infrastructure.external.ai;

import com.urisik.backend.domain.recipe.dto.RecipeStepDTO;

import java.util.List;

public final class GeminiImagePromptBuilder {

    private GeminiImagePromptBuilder() {}

    public static String recipeImage(String title, List<String> ingredients) {
        return """
        Create a realistic high-quality food photograph.
        Dish name: %s
        Main ingredients: %s
        Clean background, professional lighting, food photography style.
        """.formatted(title, String.join(", ", ingredients));
    }

    public static String stepImage(String title, RecipeStepDTO step) {
        return """
        Create a cooking step image.
        Dish: %s
        Step %d: %s
        Realistic kitchen environment, no text in image.
        """.formatted(title, step.getOrder(), step.getDescription());
    }
}

