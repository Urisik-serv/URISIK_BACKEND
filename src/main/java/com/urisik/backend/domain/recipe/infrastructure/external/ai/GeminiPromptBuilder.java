package com.urisik.backend.domain.recipe.infrastructure.external.ai;

import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.recipe.dto.res.RecipeStepDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;

import java.util.List;
import java.util.Map;

public final class GeminiPromptBuilder {

    private GeminiPromptBuilder() {}

    public static String build(
            Recipe recipe,
            List<String> ingredients,
            List<RecipeStepDTO> steps,
            Map<Allergen, List<AllergenAlternative>> rules
    ) {
        StringBuilder sb = new StringBuilder();

        /* ===== 최상단 절대 규칙 ===== */
        sb.append("""
        IMPORTANT:
        - Respond with ONLY ONE valid JSON object.
        - Do NOT include markdown, explanations, comments, or code fences.
        - Do NOT add any text before or after the JSON.
        - The response must be directly parsable by Jackson ObjectMapper.
        
        """);

        /* ===== 시스템 역할 정의 ===== */
        sb.append("""
        You are an allergy-safe recipe transformation system.
        Your role is NOT creative writing.
        Your role is STRICT TRANSFORMATION based on server-defined rules.
        You must follow the rules EXACTLY.
        
        """);

        /* ===== 원본 레시피 ===== */
        sb.append("\n[원본 레시피 제목]\n")
                .append(recipe.getTitle()).append("\n");

        sb.append("\n[원본 재료]\n");
        ingredients.forEach(i ->
                sb.append("- ").append(i).append("\n")
        );

        sb.append("\n[원본 조리 단계]\n");
        steps.forEach(s ->
                sb.append(s.getOrder())
                        .append(". ")
                        .append(s.getDescription())
                        .append("\n")
        );

        /* ===== 서버가 결정한 대체 규칙 ===== */
        sb.append("\n[알레르기 대체 규칙]\n");
        rules.forEach((allergen, alternatives) -> {
            for (AllergenAlternative alt : alternatives) {
                sb.append("- ")
                        .append(allergen.name())
                        .append(" (")
                        .append(allergen.getKoreanName())
                        .append(")")
                        .append(" → ")
                        .append(alt.getIngredient().getName())
                        .append(" (이유: ")
                        .append(alt.getReason())
                        .append(")\n");
            }
        });

        /* ===== 절대 규칙 ===== */
        sb.append("""
        
                [ABSOLUTE RULES]
                        1. Do NOT include any allergen listed above.
                        2. Use ONLY the specified replacement ingredients.
                        3. Do NOT introduce new allergens.
                        4. Ingredients and steps must be consistent.
                        5. Step order must start from 1 and be continuous.
        
                        ""\");
        
                        /* ===== 출력 JSON 스키마 ===== */
                        sb.append(""\"
                        [OUTPUT JSON SCHEMA]
                        {
                          "title": "string",
                          "ingredients": [
                            "string"
                          ],
                          "steps": [
                            { "order": 1, "description": "string" }
                          ],
                          "substitutionSummary": [
                            {
                              "allergen": "ALLERGEN_ENUM_NAME",
                              "replacedWith": "string",
                              "reason": "string"
                            }
                          ]
                        }
        """);

        return sb.toString();

    }
}

