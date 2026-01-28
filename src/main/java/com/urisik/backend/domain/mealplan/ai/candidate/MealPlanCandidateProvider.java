package com.urisik.backend.domain.mealplan.ai.candidate;

import java.util.List;

public interface MealPlanCandidateProvider {
    List<Long> getCandidateRecipeIds(Long familyRoomId);
}
