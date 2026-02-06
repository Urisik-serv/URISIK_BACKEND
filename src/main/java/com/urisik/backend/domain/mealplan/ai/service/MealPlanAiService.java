package com.urisik.backend.domain.mealplan.ai.service;

import com.urisik.backend.domain.mealplan.ai.parser.MealPlanAiResponseParser;
import com.urisik.backend.domain.mealplan.ai.prompt.MealPlanPromptBuilder;
import com.urisik.backend.domain.mealplan.ai.validation.MealPlanGenerationValidator;
import com.urisik.backend.domain.mealplan.dto.req.RecipeSelectionDTO;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
import com.urisik.backend.global.ai.AiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
        log.info("[AI][FLOW] MealPlanAiService.generate start (slots={}, candidates={}, aiClient={})",
                selectedSlots == null ? 0 : selectedSlots.size(),
                candidateSelections == null ? 0 : candidateSelections.size(),
                aiClient == null ? "null" : aiClient.getClass().getSimpleName());

        String prompt = promptBuilder.build(selectedSlots, candidateSelections);
        log.info("[AI][FLOW] prompt built (chars={})", prompt == null ? 0 : prompt.length());

        String json;
        try {
            log.info("[AI][FLOW] calling aiClient.generateJson (client={})",
                    aiClient == null ? "null" : aiClient.getClass().getSimpleName());
            json = aiClient.generateJson(prompt);
            log.info("[AI][FLOW] aiClient.generateJson success (jsonChars={})", json == null ? 0 : json.length());
        } catch (Exception e) {
            log.error("[AI][FLOW] aiClient.generateJson FAILED (client={}, errType={}, msg={})",
                    aiClient == null ? "null" : aiClient.getClass().getSimpleName(),
                    e.getClass().getSimpleName(),
                    e.getMessage());
            throw e;
        }

        Map<MealPlan.SlotKey, RecipeSelectionDTO> assignments =
                responseParser.parseSelections(json, selectedSlots, candidateSelections);
        log.info("[AI][FLOW] parsed assignments (count={})", assignments == null ? 0 : assignments.size());

        validator.validateRecipeSelections(
                selectedSlots,
                assignments,
                candidateSelections
        );

        log.info("[AI][FLOW] MealPlanAiService.generate done");
        return assignments;
    }
}
