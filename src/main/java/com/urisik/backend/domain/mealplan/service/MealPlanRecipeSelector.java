package com.urisik.backend.domain.mealplan.service;

import com.urisik.backend.domain.mealplan.dto.req.RecipeSelectionDTO;
import com.urisik.backend.domain.mealplan.exception.MealPlanException;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanErrorCode;
import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import com.urisik.backend.domain.recipe.service.AllergyRiskService;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealPlanRecipeSelector {

    private final TransformedRecipeRepository transformedRecipeRepository;
    private final AllergyRiskService allergyRiskService;

    /**
     * 어떤 선택(RECIPE/TRANSFORMED_RECIPE)이 오든,
     * 최종적으로 MealPlan에 저장할 transformedRecipeId를 반환
     */
    @Transactional
    public Long resolveToTransformedRecipeId(Long familyRoomId, RecipeSelectionDTO selection) {

        if (selection.type() == RecipeSelectionDTO.RecipeSelectionType.RECIPE) {
            // recipeId -> 우리 가족 transformed 확보
            return transformedRecipeRepository
                    .findByRecipe_IdAndFamilyRoomId(selection.id(), familyRoomId)
                    .map(TransformedRecipe::getId)
                    .orElseThrow(() -> new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED));
        }

        // transformedRecipeId 선택
        TransformedRecipe tr = transformedRecipeRepository.findById(selection.id())
                .orElseThrow(() -> new GeneralException(RecipeErrorCode.TRANSFORMED_RECIPE_NOT_FOUND));

        Long baseRecipeId = tr.getRecipe().getId();

        // 내 가족이 만든 transformed면 그대로 사용
        if (tr.getFamilyRoomId().equals(familyRoomId)) {
            return tr.getId();
        }

        // 다른 가족 transformed라면 내 가족 기준 안전 검사
        List<String> ingredients = RecipeTextParser.parseIngredients(tr.getIngredientsTransformed());
        if (!allergyRiskService.detectRiskAllergens(familyRoomId, ingredients).isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_RECIPE_NOT_SAFE);
        }

        // 안전하면 그냥 재사용
        return tr.getId();
    }
}