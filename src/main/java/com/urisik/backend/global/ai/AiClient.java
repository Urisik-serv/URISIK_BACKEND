package com.urisik.backend.global.ai;

public interface AiClient {

    /**
     * Generate a JSON-only response string from the configured AI provider.
     * The returned string should be the raw assistant output text (JSON object as string).
     */
    String generateJson(String prompt);
}
