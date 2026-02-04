package com.urisik.backend.domain.recipe.infrastructure.ai;

import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.recipe.entity.Recipe;

import java.util.Map;

public class RecipeTransformPromptBuilder {

    private RecipeTransformPromptBuilder() {}

    public static String build(
            Recipe baseRecipe,
            Map<Allergen, AllergenAlternative> primary
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
                너는 알레르기 안전 레시피를 생성하는 전문가 AI다.
                너의 역할은 기존 레시피를 수정하여 새로운 레시피를 작성하는 것이다.
                판단이나 재료 선택은 하지 말고, 주어진 규칙만 따라라.

                ========================
                [원본 레시피]
                ========================
                """);

        sb.append("제목:\n")
                .append(baseRecipe.getTitle()).append("\n\n");

        sb.append("재료:\n")
                .append(baseRecipe.getIngredientsRaw()).append("\n\n");

        sb.append("조리 단계:\n")
                .append(baseRecipe.getInstructionsRaw()).append("\n\n");

        sb.append("""
                ========================
                [알레르기 대체 규칙]
                ========================
                아래 규칙은 반드시 지켜야 한다.
                임의로 다른 재료를 선택하거나 추가하지 마라.
                """);

        for (AllergenAlternative alt : primary.values()) {
            sb.append("- ")
                    .append(alt.getAllergen().getKoreanName())
                    .append(" (").append(alt.getAllergen().name()).append(")")
                    .append(" → 반드시 '")
                    .append(alt.getIngredient().getName())
                    .append("' 로 대체\n")
                    .append("  이유: ")
                    .append(alt.getReason())
                    .append("\n");
        }

        sb.append("""
                
                ========================
                [출력 형식]
                ========================
                아래 JSON 형식으로만 출력하라.
                JSON 이외의 텍스트는 절대 출력하지 마라.

                {
                  "title": string,
                  "ingredients": [string],
                  "steps": [
                    { "order": number, "description": string }
                  ]
                }
                """);

        return sb.toString();
    }
}

