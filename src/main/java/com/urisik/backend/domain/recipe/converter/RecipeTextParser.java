package com.urisik.backend.domain.recipe.converter;

import com.urisik.backend.domain.recipe.dto.RecipeStepDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeTextParser {

    private RecipeTextParser() {}

    public static List<String> parseIngredients(String raw) {
        if (raw == null || raw.isBlank()) return List.of();

        String normalized = raw.replace("\r", "\n");

        return Arrays.stream(normalized.split("[,\n]"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    public static List<RecipeStepDTO> parseSteps(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        String normalized = raw.replace("\r", "\n");
        List<String> lines = Arrays.stream(normalized.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        List<RecipeStepDTO> steps = new ArrayList<>();
        int order = 1;
        for (String line : lines) {
            steps.add(new RecipeStepDTO(order++, line));
        }
        return steps;
    }
}

