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

    private static final Pattern STEP_PATTERN =
            Pattern.compile("(\\d+)[\\.\\)]\\s*(.*?)(?=(\\d+[\\.\\)])|$)", Pattern.DOTALL);

    public static List<RecipeStepDTO> parseSteps(String raw) {

        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        String normalized = raw.replace("\r", "\n").trim();

        List<RecipeStepDTO> steps = new ArrayList<>();

        Matcher matcher = STEP_PATTERN.matcher(normalized);

        while (matcher.find()) {
            int order = Integer.parseInt(matcher.group(1));
            String description = matcher.group(2).trim();

            if (!description.isBlank()) {
                steps.add(new RecipeStepDTO(order, description));
            }
        }

        // 번호 패턴이 없는 경우 fallback (줄 기준 분리)
        if (steps.isEmpty()) {
            String[] lines = normalized.split("\n");
            int order = 1;

            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isBlank()) {
                    steps.add(new RecipeStepDTO(order++, trimmed));
                }
            }
        }

        return steps;
    }

}

