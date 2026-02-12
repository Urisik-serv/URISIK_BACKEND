package com.urisik.backend.global.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@ConditionalOnExpression("!T(org.springframework.util.StringUtils).hasText('${gemini.api-key:}')")
@Component
@Slf4j
public class NoAiClient implements AiClient {

    public NoAiClient() {
        log.warn("[AI][BOOT] NoAiClient ENABLED (Gemini API key not configured)");
    }

    @Override
    public String generateJson(String prompt) {
        log.warn("[AI][CALL] NoAiClient generateJson called – AI disabled");
        throw new IllegalStateException("AI is not configured");
    }
}
