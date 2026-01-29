package com.urisik.backend.domain.mealplan.validation;

import org.springframework.stereotype.Component;

/**
 * TODO(리팩터링): 가족방 구성원 알레르기 + 레시피 알레르기/재료 매핑 붙으면 구현 교체
 * 현재는 항상 통과
 */
@Component
public class DummyMealPlanSafetyValidator implements MealPlanSafetyValidator {

    @Override
    public void validateFamilySafe(Long familyRoomId, Long recipeId) {
    }
}
