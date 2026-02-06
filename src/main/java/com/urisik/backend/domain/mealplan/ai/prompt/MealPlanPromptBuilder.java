package com.urisik.backend.domain.mealplan.ai.prompt;

import com.urisik.backend.domain.mealplan.dto.common.RecipeSelectionDTO;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MealPlanPromptBuilder {

    public String build(
            List<MealPlan.SlotKey> slots,
            List<RecipeSelectionDTO> candidateRecipeIds
    ) {
        String slotKeys = slots.stream()
                .map(MealPlanPromptBuilder::toCanonicalPromptKey)
                .collect(Collectors.joining(", "));

        String candidateIds = candidateRecipeIds.stream()
                .map(s -> "{\"type\": \"%s\", \"id\": %d}".formatted(s.type().name(), s.id()))
                .collect(Collectors.joining(", "));

        return """
        You are a meal plan generator.

        Rules:
        - Respond in JSON only
        - Keys must be in the form MEALTYPE_DAYOFWEEK (e.g., LUNCH_MONDAY, DINNER_SUNDAY)
        - Use ONLY the provided slot keys; do not invent new keys
        - Each value must be an object with fields: { "type": string, "id": number }
        - type must be one of: RECIPE or TRANSFORMED_RECIPE
        - id must be chosen from candidateRecipeIds
        - Do not explain anything

        Slots (allowed keys):
        [%s]

        candidateRecipeIds:
        [%s]
        """.formatted(slotKeys, candidateIds);
    }

    private static final Set<String> MEAL_TYPES = Set.of("LUNCH", "DINNER");

    /**
     * Convert various slot key renderings into the canonical prompt key format: MEALTYPE_DAYOFWEEK.
     * Examples:
     * - LUNCH_MONDAY -> LUNCH_MONDAY
     * - MONDAY_LUNCH -> LUNCH_MONDAY
     */
    private static String toCanonicalPromptKey(MealPlan.SlotKey slotKey) {
        if (slotKey == null) {
            return "";
        }

        String raw = slotKey.toString();
        if (raw == null) {
            return "";
        }

        String s = raw.trim().toUpperCase().replace('-', '_');
        String[] parts = s.split("_");
        if (parts.length != 2) {
            // Fall back to the normalized string; parser/validator will still guard
            return s;
        }

        String a = parts[0];
        String b = parts[1];

        // If already MEALTYPE_DAYOFWEEK
        if (MEAL_TYPES.contains(a)) {
            return a + "_" + b;
        }

        // If DAYOFWEEK_MEALTYPE, swap to MEALTYPE_DAYOFWEEK
        if (MEAL_TYPES.contains(b)) {
            return b + "_" + a;
        }

        // Unknown pattern: return normalized
        return s;
    }
}
