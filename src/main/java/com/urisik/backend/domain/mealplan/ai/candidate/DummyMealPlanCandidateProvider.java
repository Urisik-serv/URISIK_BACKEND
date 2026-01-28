package com.urisik.backend.domain.mealplan.ai.candidate;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DummyMealPlanCandidateProvider implements MealPlanCandidateProvider {

    @Override
    public List<Long> getCandidateRecipeIds(Long familyRoomId) {
        // TODO(리팩터링): family_wishlist + recipe DB로 교체
        return List.of(1001L, 1002L, 1003L, 1004L, 1005L, 1006L);
    }
}
