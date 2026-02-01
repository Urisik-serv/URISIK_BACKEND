package com.urisik.backend.domain.recipe.converter;

import com.urisik.backend.domain.recipe.dto.res.RecipeStepDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeTextParser {

    private RecipeTextParser() {}

    public static List<String> parseIngredients(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        // "김치 200g, 돼지고기 150g" or 줄바꿈 혼용 대응
        String normalized = raw.replace("\r", "\n");
        String[] tokens = normalized.contains(",")
                ? normalized.split(",")
                : normalized.split("\n");

        List<String> result = new ArrayList<>();
        for (String t : tokens) {
            String s = t.trim();
            if (!s.isBlank()) result.add(s);
        }
        return result;
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

