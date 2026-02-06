package com.urisik.backend.global.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

/**
 * Google Gemini API client.
 * - model: gemini.model.default (gemini-3-flash-preview)
 * - temperature: gemini.temperature.default (0.2)
 * - Uses Gemini's generateContent endpoint.
 */
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${gemini.api-key:}')")
@Component
@Slf4j
public class GeminiClient implements AiClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final double temperature;

    public GeminiClient(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.base-url:https://generativelanguage.googleapis.com}") String baseUrl,
            @Value("${gemini.model.default:gemini-3-flash-preview}") String model,
            @Value("${gemini.temperature.default:0.2}") double temperature
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("gemini.api-key is required");
        }
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
        this.temperature = temperature;
        this.apiKey = apiKey;
        log.info("[AI][BOOT] GeminiClient ENABLED (model={}, temperature={}, baseUrl={})", this.model, this.temperature, baseUrl);
    }

    private final String apiKey;

    @Override
    public String generateJson(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt must not be blank");
        }
        log.info("[AI][CALL] Gemini generateJson called (model={}, promptChars={})", model, prompt.length());

        // Gemini expects a JSON body as string, and API key as query parameter.
        String req = "{\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"") + "\"}]}]," +
                "\"generationConfig\":{\"temperature\":" + temperature + "}}";

        String rawJson;
        try {
            rawJson = webClient
                    .post()
                    .uri("/v1beta/models/" + model + ":generateContent?key=" + apiKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(DEFAULT_TIMEOUT);
            log.info("[AI][CALL] Gemini response received (chars={})", rawJson == null ? 0 : rawJson.length());
        } catch (WebClientResponseException e) {
            throw new IllegalStateException(
                    "Gemini call failed: HTTP " + e.getStatusCode().value() + " " + e.getResponseBodyAsString(), e
            );
        } catch (Exception e) {
            throw new IllegalStateException("Gemini call failed", e);
        }

        if (rawJson == null || rawJson.isBlank()) {
            throw new IllegalStateException("Gemini returned empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode candidates = root.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        JsonNode textNode = parts.get(0).get("text");
                        if (textNode != null && textNode.isTextual() && !textNode.asText().isBlank()) {
                            log.info("[AI][CALL] Gemini text extracted (chars={})", textNode.asText().length());
                            return textNode.asText();
                        }
                    }
                }
            }
            throw new IllegalStateException("Gemini response did not contain text");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Gemini response JSON", e);
        }
    }
}
