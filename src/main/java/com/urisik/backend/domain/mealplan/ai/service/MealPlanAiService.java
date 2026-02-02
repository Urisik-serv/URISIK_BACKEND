package com.urisik.backend.domain.mealplan.ai.service;

import com.urisik.backend.global.ai.AiClient;
import com.urisik.backend.domain.mealplan.ai.parser.MealPlanAiResponseParser;
import com.urisik.backend.domain.mealplan.ai.prompt.MealPlanPromptBuilder;
import com.urisik.backend.domain.mealplan.ai.validation.MealPlanGenerationValidator;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MealPlanAiService {

    private final AiClient aiClient;
    private final MealPlanPromptBuilder promptBuilder;
    private final MealPlanGenerationValidator validator;

    private final MealPlanAiResponseParser responseParser;

    public Map<MealPlan.SlotKey, Long> generate(
            List<MealPlan.SlotKey> selectedSlots,
            List<Long> candidateRecipeIds
    ) {
        String prompt = promptBuilder.build(selectedSlots, candidateRecipeIds);
        String json = aiClient.generateJson(prompt);

        Map<MealPlan.SlotKey, Long> assignments = responseParser.parse(json, selectedSlots, candidateRecipeIds);

        validator.validateRecipeAssignments(
                selectedSlots,
                assignments,
                candidateRecipeIds
        );

        return assignments;
    }
}
