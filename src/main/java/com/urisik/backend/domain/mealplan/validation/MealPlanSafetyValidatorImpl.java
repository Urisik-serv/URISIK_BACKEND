package com.urisik.backend.domain.mealplan.validation;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import com.urisik.backend.domain.familyroom.repository.FamilyWishListExclusionRepository;
import com.urisik.backend.domain.mealplan.dto.common.RecipeSelectionDTO;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanErrorCode;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;

import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import com.urisik.backend.domain.mealplan.exception.MealPlanException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 가족방 안전 검증
 * - 가족방 전체 알레르기 기준으로 ingredientsRaw에 알레르겐이 포함되면 UNSAFE
 * - 방장 제외(exclusion) 목록에 포함된 레시피는 UNSAFE
 * NOTE: FamilyWishListQueryService의 알레르기 판별(정규화 + 부분포함 매칭)과 동일한 기준을 사용한다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealPlanSafetyValidatorImpl implements MealPlanSafetyValidator {

    private final MemberAllergyRepository memberAllergyRepository;
    private final FamilyWishListExclusionRepository familyWishListExclusionRepository;

    private final RecipeRepository recipeRepository;
    private final TransformedRecipeRepository transformedRecipeRepository;

    @Override
    public void validateFamilySafe(Long familyRoomId, RecipeSelectionDTO selection) {
        if (familyRoomId == null) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }
        if (selection == null || selection.type() == null || selection.id() == null) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        // 1) exclusion(방장 제외 목록) 검증
        if (isExcluded(familyRoomId, selection)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        // 2) 가족 알레르기 기준 검증 (ingredientsRaw 기반)
        List<String> normalizedAllergens = loadNormalizedFamilyAllergens(familyRoomId);
        if (normalizedAllergens.isEmpty()) {
            return; // 알레르기 없음 -> 통과
        }

        String ingredientsRaw = loadIngredientsRaw(selection);
        if (ingredientsRaw == null || ingredientsRaw.isBlank()) {
            // 재료정보가 없으면 안전 판정 불가 -> unsafe 처리
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        if (!isUsableForMealPlan(ingredientsRaw, normalizedAllergens)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }
    }

    private boolean isExcluded(Long familyRoomId, RecipeSelectionDTO selection) {
        if (selection.type() == RecipeSelectionDTO.RecipeSelectionType.RECIPE) {
            Set<Long> excluded = familyWishListExclusionRepository.findExcludedRecipeIdsByFamilyRoomId(familyRoomId);
            return excluded != null && excluded.contains(selection.id());
        }
        if (selection.type() == RecipeSelectionDTO.RecipeSelectionType.TRANSFORMED_RECIPE) {
            Set<Long> excluded = familyWishListExclusionRepository.findExcludedTransformedRecipeIdsByFamilyRoomId(familyRoomId);
            return excluded != null && excluded.contains(selection.id());
        }
        return false;
    }

    private List<String> loadNormalizedFamilyAllergens(Long familyRoomId) {
        List<Allergen> familyAllergens = memberAllergyRepository.findDistinctAllergensByFamilyRoomId(familyRoomId);
        if (familyAllergens == null || familyAllergens.isEmpty()) {
            return List.of();
        }

        List<String> normalized = new ArrayList<>();
        for (Allergen a : familyAllergens) {
            if (a == null) continue;
            String name = a.getKoreanName();
            String n = normalize(name);
            if (n == null || n.isBlank()) continue;
            normalized.add(n);
        }

        return normalized.stream().distinct().toList();
    }

    private String loadIngredientsRaw(RecipeSelectionDTO selection) {
        if (selection.type() == RecipeSelectionDTO.RecipeSelectionType.RECIPE) {
            Recipe recipe = recipeRepository.findById(selection.id())
                    .orElseThrow(() -> new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED));
            return recipe.getIngredientsRaw();
        }

        if (selection.type() == RecipeSelectionDTO.RecipeSelectionType.TRANSFORMED_RECIPE) {
            TransformedRecipe tr = transformedRecipeRepository.findById(selection.id())
                    .orElseThrow(() -> new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED));
            return tr.getIngredientsRaw();
        }

        throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
    }

    /**
     * usableForMealPlan 판단
     * - FamilyWishListQueryService와 동일하게: ingredientsRaw를 토큰화 후 normalize하고
     *   allergenKey가 부분포함(contains)되면 unsafe로 본다.
     */
    private boolean isUsableForMealPlan(String ingredientsRaw, List<String> normalizedAllergens) {
        if (normalizedAllergens == null || normalizedAllergens.isEmpty()) {
            return true;
        }

        if (ingredientsRaw == null || ingredientsRaw.isBlank()) {
            return false;
        }

        List<String> normalizedIngredients = splitIngredientsRaw(ingredientsRaw).stream()
                .map(this::normalize)
                .filter(s -> s != null && !s.isBlank())
                .toList();

        if (normalizedIngredients.isEmpty()) {
            return false;
        }

        for (String allergenKey : normalizedAllergens) {
            if (allergenKey == null || allergenKey.isBlank()) continue;

            boolean matched = normalizedIngredients.stream()
                    .anyMatch(ing -> ing.contains(allergenKey));

            if (matched) return false;
        }

        return true;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String lowered = s.toLowerCase(Locale.ROOT);
        return lowered.replaceAll("[^0-9a-zA-Z가-힣]", "");
    }

    private List<String> splitIngredientsRaw(String ingredientsRaw) {
        if (ingredientsRaw == null || ingredientsRaw.isBlank()) {
            return List.of();
        }

        String[] parts = ingredientsRaw.split("[\\n\\r,;/\\t]+|[·•]");

        List<String> tokens = new ArrayList<>();
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (!t.isEmpty()) tokens.add(t);
        }
        return tokens;
    }
}
