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
            Pattern.compile("(?m)^(\\d+)\\.\\s*(.*)");

    public static List<RecipeStepDTO> parseSteps(String raw) {

        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        String normalized = raw.replace("\r", "").trim();

        List<RecipeStepDTO> steps = new ArrayList<>();
        Matcher matcher = STEP_PATTERN.matcher(normalized);

        List<Integer> positions = new ArrayList<>();

        while (matcher.find()) {
            positions.add(matcher.start());
        }

        for (int i = 0; i < positions.size(); i++) {
            int start = positions.get(i);
            int end = (i + 1 < positions.size()) ? positions.get(i + 1) : normalized.length();

            String block = normalized.substring(start, end).trim();

            Matcher m = STEP_PATTERN.matcher(block);
            if (m.find()) {
                int order = Integer.parseInt(m.group(1));
                String description = block.substring(m.end()).trim();
                steps.add(new RecipeStepDTO(order, description));
            }
        }

        return steps;
    }


}

