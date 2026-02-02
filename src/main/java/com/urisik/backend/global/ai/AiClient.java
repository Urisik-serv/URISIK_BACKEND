package com.urisik.backend.global.ai;

public interface AiClient {

    /**
     * Generate a JSON-only response string using OpenAI Responses API.
     * The returned string should be the assistant output text (JSON object as string).
     */
    String generateJson(String prompt);
}
