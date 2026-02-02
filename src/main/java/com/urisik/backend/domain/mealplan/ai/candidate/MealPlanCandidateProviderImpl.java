package com.urisik.backend.domain.mealplan.ai.candidate;

import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MealPlanCandidateProviderImpl implements MealPlanCandidateProvider {

    private final TransformedRecipeRepository transformedRecipeRepository;

    /**
     * 실제용
     */
//    @Override
//    public List<Long> getCandidateRecipeIds(Long familyRoomId) {
//
//        return transformedRecipeRepository.findByFamilyRoomId(familyRoomId).stream()
//                .map(tr -> tr.getRecipe().getId())
//                .filter(Objects::nonNull)
//                .distinct()
//                .toList();
//    }

    /**
     * 테스트옹
     */
    private final RecipeRepository recipeRepository;

    @Override
    public List<Long> getCandidateRecipeIds(Long familyRoomId) {
        return recipeRepository.findAll()
                .stream()
                .map(Recipe::getId)
                .toList();
        }
}