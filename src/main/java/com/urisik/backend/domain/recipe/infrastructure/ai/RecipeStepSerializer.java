package com.urisik.backend.domain.recipe.infrastructure.ai;

import java.util.List;

public class RecipeStepSerializer {

    private RecipeStepSerializer() {}

    public static String serialize(List<AiTransformedRecipePayload.Step> steps) {
        if (steps == null || steps.isEmpty()) {
            return "1. (생성 실패)";
        }

        StringBuilder sb = new StringBuilder();
        int index = 1;

        for (AiTransformedRecipePayload.Step step : steps) {
            if (step == null || step.getDescription() == null) continue;

            int order = step.getOrder() != null ? step.getOrder() : index;
            sb.append(order)
                    .append(". ")
                    .append(step.getDescription().trim())
                    .append("\n");

            index++;
        }

        return sb.toString().trim();
    }
}

