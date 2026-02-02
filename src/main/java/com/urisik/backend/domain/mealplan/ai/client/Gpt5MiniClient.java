
package com.urisik.backend.domain.mealplan.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.time.Duration;
import java.util.List;

/**
 * OpenAI Responses API client for MealPlan generation.
 * - model: gpt-5.0-mini
 * - temperature: 0~0.3
 * - JSON only (uses Responses API text.format json_object)
 */
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${openai.api-key:}')")
@Component
public class Gpt5MiniClient implements AiClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final double temperature;

    public Gpt5MiniClient(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.base-url:https://api.openai.com}") String baseUrl,
            @Value("${openai.model.mealplan:gpt-5.0-mini}") String model,
            @Value("${openai.temperature.mealplan:0.2}") double temperature
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("openai.api-key is required");
        }
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
        this.temperature = temperature;
    }

    @Override
    public String generate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt must not be blank");
        }

        // Responses API request
        // POST /v1/responses
        // {
        //   "model": "gpt-5.0-mini",
        //   "input": [{"role":"user","content":"..."}],
        //   "temperature": 0.2,
        //   "text": {"format": {"type": "json_object"}}
        // }
        ResponseCreateRequest req = new ResponseCreateRequest(
                model,
                List.of(new InputMessage("user", prompt)),
                temperature,
                new TextConfig(new TextFormat("json_object"))
        );

        String rawJson;
        try {
            rawJson = webClient
                    .post()
                    .uri("/v1/responses")
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse
                                    .bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> new IllegalStateException(
                                            "OpenAI Responses API error: HTTP " + clientResponse.statusCode().value() + " " + body
                                    ))
                    )
                    .bodyToMono(String.class)
                    .block(DEFAULT_TIMEOUT);
        } catch (WebClientResponseException e) {
            // network/protocol-level errors
            throw new IllegalStateException("OpenAI call failed: HTTP " + e.getStatusCode().value() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new IllegalStateException("OpenAI call failed", e);
        }

        if (rawJson == null || rawJson.isBlank()) {
            throw new IllegalStateException("OpenAI returned empty response");
        }

        // Prefer `output_text` convenience field if present.
        // Otherwise, fall back to extracting from output[].
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            JsonNode outputText = root.get("output_text");
            if (outputText != null && outputText.isTextual() && !outputText.asText().isBlank()) {
                return outputText.asText();
            }

            // Fallback: try to locate the first assistant message content text
            JsonNode output = root.get("output");
            if (output != null && output.isArray()) {
                for (JsonNode item : output) {
                    if (item == null) continue;
                    if (!"message".equals(item.path("type").asText())) continue;

                    JsonNode content = item.get("content");
                    if (content == null || !content.isArray()) continue;

                    for (JsonNode c : content) {
                        // According to Responses API, assistant text parts use type "output_text" with a "text" field.
                        if (c == null) continue;
                        if ("output_text".equals(c.path("type").asText())) {
                            String text = c.path("text").asText(null);
                            if (text != null && !text.isBlank()) {
                                return text;
                            }
                        }
                        // Defensive fallback if the API changes naming
                        String maybeText = c.path("text").asText(null);
                        if (maybeText != null && !maybeText.isBlank()) {
                            return maybeText;
                        }
                    }
                }
            }

            throw new IllegalStateException("OpenAI response did not contain output_text");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAI response JSON", e);
        }
    }

    // ====== DTOs for Responses API ======

    /** Request body for POST /v1/responses */
    public record ResponseCreateRequest(
            String model,
            List<InputMessage> input,
            Double temperature,
            TextConfig text
    ) {
    }

    /** Input item (message) */
    public record InputMessage(
            String role,
            String content
    ) {
    }

    public record TextConfig(
            TextFormat format
    ) {
    }

    public record TextFormat(
            String type
    ) {
    }
}
