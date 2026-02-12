package com.urisik.backend.domain.mealplan.ai.service;

import com.urisik.backend.domain.mealplan.ai.parser.MealPlanAiResponseParser;
import com.urisik.backend.domain.mealplan.ai.prompt.MealPlanPromptBuilder;
import com.urisik.backend.domain.mealplan.ai.validation.MealPlanGenerationValidator;
import com.urisik.backend.domain.mealplan.dto.common.RecipeSelectionDTO;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
import com.urisik.backend.global.ai.AiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealPlanAiService {

    private final AiClient aiClient;
    private final MealPlanPromptBuilder promptBuilder;
    private final MealPlanGenerationValidator validator;
    private final MealPlanAiResponseParser responseParser;

    public Map<MealPlan.SlotKey, RecipeSelectionDTO> generate(
            List<MealPlan.SlotKey> selectedSlots,
            List<RecipeSelectionDTO> candidateSelections
    ) {
        final long t0 = System.nanoTime();
        final int slotCount = selectedSlots == null ? 0 : selectedSlots.size();
        final int candidateCount = candidateSelections == null ? 0 : candidateSelections.size();
        final String clientName = aiClient == null ? "null" : aiClient.getClass().getSimpleName();

        log.info("[AI][FLOW] MealPlanAiService.generate start (slots={}, candidates={}, aiClient={})",
                slotCount,
                candidateCount,
                clientName);

        // Prompt build timing
        final long tPrompt0 = System.nanoTime();
        String prompt = promptBuilder.build(selectedSlots, candidateSelections);
        final long promptMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - tPrompt0);
        log.info("[AI][FLOW] prompt built (chars={}, promptMs={})", prompt == null ? 0 : prompt.length(), promptMs);

        // AI call timing (Gemini / external) with MealPlan-specific timeout (20s)
        String json;
        long aiMs;
        try {
            log.info("[AI][FLOW] calling aiClient.generateJson (client={}, timeout=20s)", clientName);
            final long tAi0 = System.nanoTime();

            json = CompletableFuture
                    .supplyAsync(() -> aiClient.generateJson(prompt))
                    .orTimeout(20, TimeUnit.SECONDS)
                    .join();

            aiMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - tAi0);
            log.info("[AI][FLOW] aiClient.generateJson success (jsonChars={}, aiMs={})",
                    json == null ? 0 : json.length(),
                    aiMs);

        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof TimeoutException) {
                log.warn("[AI][FLOW] aiClient.generateJson TIMEOUT (client={}, timeout=20s)",
                        clientName);
            } else {
                log.error("[AI][FLOW] aiClient.generateJson FAILED (client={}, errType={}, msg={})",
                        clientName,
                        e.getClass().getSimpleName(),
                        e.getMessage());
            }

            final long totalMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
            log.warn("[PERF] mealplan_ai_generate totalMs={} promptMs={} aiMs={} slots={} candidates={} aiClient={} success=false",
                    totalMs,
                    promptMs,
                    -1,
                    slotCount,
                    candidateCount,
                    clientName);
            throw e;
        }

        // Parse timing
        final long tParse0 = System.nanoTime();
        Map<MealPlan.SlotKey, RecipeSelectionDTO> assignments =
                responseParser.parseSelections(json, selectedSlots, candidateSelections);
        final long parseMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - tParse0);
        log.info("[AI][FLOW] parsed assignments (count={}, parseMs={})", assignments == null ? 0 : assignments.size(), parseMs);

        // Validation timing
        final long tVal0 = System.nanoTime();
        validator.validateRecipeSelections(
                selectedSlots,
                assignments,
                candidateSelections
        );
        final long validateMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - tVal0);

        final long totalMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);

        // One-line PERF summary for before/after comparison
        log.info("[PERF] mealplan_ai_generate totalMs={} promptMs={} aiMs={} parseMs={} validateMs={} slots={} candidates={} aiClient={} success=true",
                totalMs,
                promptMs,
                aiMs,
                parseMs,
                validateMs,
                slotCount,
                candidateCount,
                clientName);

        log.info("[AI][FLOW] MealPlanAiService.generate done");
        return assignments;
    }

    public String getAiClient() {
        return aiClient == null ? "NoAiClient" : aiClient.getClass().getSimpleName();
    }

    public boolean isAiUsed() {
        return aiClient != null && !(aiClient instanceof com.urisik.backend.global.ai.NoAiClient);
    }
}
