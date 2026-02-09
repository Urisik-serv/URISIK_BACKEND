package com.urisik.backend.global.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiImageClient implements AiImageClient {

    private final WebClient geminiImageWebClient;
    private final ObjectMapper objectMapper;

    @Value("${google.gemini.api-key}")
    private String imageApiKey;

    @Value("${google.gemini.model}")
    private String imageModel;

    @Override
    public Optional<byte[]> generateImage(String prompt) {
        try {
            String requestJson = buildRequest(prompt);

            String response = geminiImageWebClient.post()
                    .uri("/v1beta/models/{model}:generateContent", imageModel)
                    .header("x-goog-api-key", imageApiKey)
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                return Optional.empty();
            }

            return extractImageBytes(response);

        } catch (Exception e) {
            log.warn("[AI][IMAGE][REST] generation failed", e);
            return Optional.empty();
        }
    }

    private String buildRequest(String prompt) throws JsonProcessingException {
        ObjectNode body = objectMapper.createObjectNode();

        body.putArray("contents")
                .addObject()
                .putArray("parts")
                .addObject()
                .put("text", prompt);

        body.putObject("generationConfig")
                .putArray("response_modalities")
                .add("TEXT")
                .add("IMAGE");

        return objectMapper.writeValueAsString(body);
    }

    private Optional<byte[]> extractImageBytes(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            for (JsonNode candidate : root.path("candidates")) {
                for (JsonNode part : candidate.path("content").path("parts")) {
                    JsonNode inlineData = part.get("inlineData");
                    if (inlineData != null && inlineData.has("data")) {
                        return Optional.of(
                                Base64.getDecoder()
                                        .decode(inlineData.get("data").asText())
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[AI][IMAGE][REST] parse failed", e);
        }
        return Optional.empty();
    }
}
