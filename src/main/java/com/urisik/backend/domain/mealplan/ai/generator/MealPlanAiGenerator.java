package com.urisik.backend.domain.mealplan.ai.generator;

import com.urisik.backend.domain.mealplan.ai.service.MealPlanAiService;
import com.urisik.backend.domain.mealplan.ai.validation.MealPlanGenerationValidator;
import com.urisik.backend.domain.mealplan.dto.common.RecipeSelectionDTO;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * gemini-3-flash 기반 주간 식단 생성기
 * - MealPlanAiService 를 통해 gemini-3-flash 호출
 * - 슬롯별 선택( id + type ) 결과를 받아 검증
 * - 실패 시 상위 레이어에서 fallback 처리 가능하도록 예외 위임
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class MealPlanAiGenerator implements MealPlanGenerator {

    private final MealPlanAiService mealPlanAiService;
    private final MealPlanGenerationValidator validator;

    @Override
    public Map<MealPlan.SlotKey, RecipeSelectionDTO> generateRecipeAssignments(
            List<MealPlan.SlotKey> selectedSlots,
            List<RecipeSelectionDTO> candidateSelections
    ) {
        try {
            return mealPlanAiService.generate(
                    selectedSlots,
                    candidateSelections
            );
        } catch (Exception e) {
            // Propagate so the caller can apply fallback and report accurate metadata
            log.warn("AI meal plan generation failed; delegating fallback handling to upper layer", e);
            throw e;
        }
    }
}
