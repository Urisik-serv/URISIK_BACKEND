package com.urisik.backend.domain.mealplan.ai.client;

import com.urisik.backend.domain.mealplan.exception.MealPlanException;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@ConditionalOnExpression("!T(org.springframework.util.StringUtils).hasText('${openai.api-key:}')")
@Component
public class NoAiClient implements AiClient {
    @Override
    public String generate(String prompt) {
        throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED);
    }
}
