package com.urisik.backend.domain.recipe.infrastructure.external.ai;

public final class AiJsonExtractor {

    private AiJsonExtractor() {}

    public static String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start < 0 || end < start) {
            throw new IllegalStateException("Gemini JSON not found");
        }

        return text.substring(start, end + 1);
    }
}

