package com.urisik.backend.domain.recipe.converter;

import com.urisik.backend.domain.recipe.dto.RecipeStepDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        String normalized = raw.replace("\r", "").trim();

        List<RecipeStepDTO> steps = new ArrayList<>();

        // 숫자. 기준으로 split
        String[] parts = normalized.split("(?=\\d+\\.\\s)");

        for (String part : parts) {
            part = part.trim();
            if (part.isBlank()) continue;

            int dotIndex = part.indexOf(".");
            if (dotIndex == -1) continue;

            try {
                int order = Integer.parseInt(part.substring(0, dotIndex).trim());
                String description = part.substring(dotIndex + 1).trim();

                if (!description.isBlank()) {
                    steps.add(new RecipeStepDTO(order, description));
                }

            } catch (NumberFormatException ignored) {}
        }

        return steps;
    }
}

