package com.urisik.backend.domain.mealplan.ai.generator;

import com.urisik.backend.domain.mealplan.ai.service.MealPlanAiService;
import com.urisik.backend.domain.mealplan.ai.validation.MealPlanGenerationValidator;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * GPT-5.0 mini 기반 주간 식단 생성기
 * - MealPlanAiService 를 통해 GPT-5.0 mini 호출
 * - 슬롯별 recipeId 매핑 결과를 받아 검증
 * - 실패 시 상위 레이어에서 fallback 처리 가능하도록 예외 위임
 */
@Primary
@Component
@RequiredArgsConstructor
public class MealPlanAiGenerator implements MealPlanGenerator {

    private final MealPlanAiService mealPlanAiService;
    private final MealPlanGenerationValidator validator;
    private final MealPlanDefaultGenerator fallbackGenerator;

    @Override
    public Map<MealPlan.SlotKey, Long> generateRecipeAssignments(
            List<MealPlan.SlotKey> selectedSlots,
            List<Long> candidateRecipeIds
    ) {
        try {
            return mealPlanAiService.generate(
                    selectedSlots,
                    candidateRecipeIds
            );
        } catch (Exception e) {
            // AI 실패 → fallback
            return fallbackGenerator.generateRecipeAssignments(
                    selectedSlots,
                    candidateRecipeIds
            );
        }
    }
}
