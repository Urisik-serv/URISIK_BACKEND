package com.urisik.backend.domain.mealplan.ai.candidate;

import com.urisik.backend.domain.mealplan.dto.common.RecipeSelectionDTO;

import java.util.List;

public interface MealPlanCandidateProvider {

    List<RecipeSelectionDTO> getWishRecipeSelections(Long memberId, Long familyRoomId);

    List<RecipeSelectionDTO> getFallbackRecipeSelections(Long memberId, Long familyRoomId);
}
