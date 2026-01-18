package com.urisik.backend.global.util;

import org.springframework.stereotype.Component;

@Component
public class IngredientNormalizer {

    public String normalize(String raw) {
        if (raw == null) return null;

        return raw
                .replaceAll("\\([^)]*\\)", "")
                .replaceAll("[0-9]+.*$", "")
                .trim();
    }

}