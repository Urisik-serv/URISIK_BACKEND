package com.urisik.backend.global.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class IngredientParser {

    private final IngredientNormalizer normalizer;

    private static final Pattern LABEL_PREFIX =
            Pattern.compile("^\\s*[-•]?\\s*(소스|고명|곁들임채소|양념)\\s*:\\s*");

    public IngredientParser(IngredientNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public List<String> parseIngredients(String raw) {
        if (raw == null || raw.isBlank()) return List.of();

        return Arrays.stream(raw.split("[,\\n]"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> LABEL_PREFIX.matcher(s).replaceFirst(""))
                .map(normalizer::normalize)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }
}
