package com.urisik.backend.domain.mealplan.ai.prompt;

import com.urisik.backend.domain.mealplan.entity.MealPlan;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MealPlanPromptBuilder {

    public String build(
            List<MealPlan.SlotKey> slots,
            List<Long> candidateRecipeIds
    ) {
        return """
        You are a meal plan generator.

        Rules:
        - Respond in JSON only
        - Keys must be in the form MEALTYPE_DAYOFWEEK (e.g., LUNCH_MONDAY, DINNER_SUNDAY)
        - Use ONLY the provided slot keys; do not invent new keys
        - Values must be chosen from candidateRecipeIds
        - Do not explain anything

        Slots:
        %s

        candidateRecipeIds:
        %s
        """.formatted(slots, candidateRecipeIds);
    }
}
