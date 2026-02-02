package com.urisik.backend.global.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@ConditionalOnExpression("!T(org.springframework.util.StringUtils).hasText('${openai.api-key:}')")
@Component
public class NoAiClient implements AiClient {

    @Override
    public String generateJson(String prompt) {
        throw new IllegalStateException("AI is not configured");
    }
}
