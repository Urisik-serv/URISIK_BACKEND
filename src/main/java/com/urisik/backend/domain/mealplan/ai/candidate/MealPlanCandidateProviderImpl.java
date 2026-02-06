package com.urisik.backend.domain.mealplan.ai.candidate;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import com.urisik.backend.domain.familyroom.dto.res.FamilyWishListItemResDTO;
import com.urisik.backend.domain.familyroom.repository.FamilyWishListExclusionRepository;
import com.urisik.backend.domain.familyroom.service.FamilyWishListQueryService;
import com.urisik.backend.domain.mealplan.dto.req.RecipeSelectionDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MealPlanCandidateProviderImpl implements MealPlanCandidateProvider {

    private final FamilyWishListQueryService familyWishListQueryService;
    private final TransformedRecipeRepository transformedRecipeRepository;
    private final RecipeRepository recipeRepository;
    private final MemberAllergyRepository memberAllergyRepository;
    private final FamilyWishListExclusionRepository familyWishListExclusionRepository;

    /** 가족 위시리스트 후보 제공 */
    @Override
    public List<RecipeSelectionDTO> getWishRecipeSelections(Long memberId, Long familyRoomId) {

        List<FamilyWishListItemResDTO> wishItems =
                familyWishListQueryService.getFamilyWishList(memberId, familyRoomId);

        if (wishItems == null || wishItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<RecipeSelectionDTO> result = new ArrayList<>();
        Set<Long> usedBaseKeys = new HashSet<>();

        for (FamilyWishListItemResDTO item : wishItems) {
            // transformed 우선
            if (item.getTransformedRecipeId() != null) {
                Long baseKey = resolveBaseKeyForTransformed(item.getTransformedRecipeId());
                if (baseKey == null) continue;
                if (!usedBaseKeys.add(baseKey)) continue;

                result.add(new RecipeSelectionDTO(
                        RecipeSelectionDTO.RecipeSelectionType.TRANSFORMED_RECIPE,
                        item.getTransformedRecipeId(),
                        baseKey
                ));
                continue;
            }

            if (item.getRecipeId() != null) {
                Long baseKey = item.getRecipeId();
                if (!usedBaseKeys.add(baseKey)) continue;

                result.add(new RecipeSelectionDTO(
                        RecipeSelectionDTO.RecipeSelectionType.RECIPE,
                        item.getRecipeId(),
                        item.getRecipeId()
                ));
            }
        }

        return result;
    }

    /** fallback 후보 제공 (가족 변형 + 원형) */
    @Override
    public List<RecipeSelectionDTO> getFallbackRecipeSelections(Long memberId, Long familyRoomId) {

        // wish 후보에서 이미 사용된 baseKey는 fallback에서 제외 (위시 후보 로직 재사용)
        Set<Long> excludedBaseKeys = collectBaseKeys(getWishRecipeSelections(memberId, familyRoomId));

        // 가족방 전체 알레르기 조회 + 정규화(위시리스트 로직과 동일한 방식)
        List<Allergen> familyAllergens =
                memberAllergyRepository.findDistinctAllergensByFamilyRoomId(familyRoomId);

        List<String> normalizedAllergens = familyAllergens.stream()
                .filter(Objects::nonNull)
                .map(Allergen::getKoreanName)
                .filter(Objects::nonNull)
                .map(this::normalize)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();

        // 방장 exclusion(조회에서만 제외)도 후보군에서 제외
        Set<Long> excludedRecipeIds =
                familyWishListExclusionRepository.findExcludedRecipeIdsByFamilyRoomId(familyRoomId);
        Set<Long> excludedTransformedRecipeIds =
                familyWishListExclusionRepository.findExcludedTransformedRecipeIdsByFamilyRoomId(familyRoomId);

        List<RecipeSelectionDTO> pool = new ArrayList<>();

        // 1. 가족이 만든 변형 레시피
        List<TransformedRecipe> transformedRecipes =
                transformedRecipeRepository.findByFamilyRoomId(familyRoomId);

        for (TransformedRecipe tr : transformedRecipes) {
            if (tr == null || tr.getId() == null) continue;
            if (excludedTransformedRecipeIds != null && excludedTransformedRecipeIds.contains(tr.getId())) continue;

            // 알레르기 필터링: unsafe(알레르기 포함)면 제외
            if (!isUsableForMealPlan(tr.getIngredientsRaw(), normalizedAllergens)) continue;

            Long baseKey = resolveBaseKeyForTransformedEntity(tr);
            if (baseKey == null) continue;
            if (excludedBaseKeys.contains(baseKey)) continue;

            pool.add(new RecipeSelectionDTO(
                    RecipeSelectionDTO.RecipeSelectionType.TRANSFORMED_RECIPE,
                    tr.getId(),
                    baseKey
            ));
        }

        // 2. 원형 레시피
        List<Recipe> recipes = recipeRepository.findAll();

        for (Recipe recipe : recipes) {
            if (recipe == null || recipe.getId() == null) continue;
            if (excludedRecipeIds != null && excludedRecipeIds.contains(recipe.getId())) continue;

            // 알레르기 필터링: unsafe(알레르기 포함)면 제외
            if (!isUsableForMealPlan(recipe.getIngredientsRaw(), normalizedAllergens)) continue;

            Long baseKey = recipe.getId();
            if (excludedBaseKeys.contains(baseKey)) continue;

            pool.add(new RecipeSelectionDTO(
                    RecipeSelectionDTO.RecipeSelectionType.RECIPE,
                    recipe.getId(),
                    recipe.getId()
            ));
        }

        // 랜덤 + baseRecipe 중복 제거
        Collections.shuffle(pool);

        List<RecipeSelectionDTO> result = new ArrayList<>();
        Set<Long> usedBaseKeys = new HashSet<>();

        for (RecipeSelectionDTO selection : pool) {
            Long baseKey = resolveBaseKey(selection);
            if (baseKey == null) continue;
            if (!usedBaseKeys.add(baseKey)) continue;

            result.add(selection);
        }

        return result;
    }

    private Set<Long> collectBaseKeys(List<RecipeSelectionDTO> selections) {
        if (selections == null || selections.isEmpty()) {
            return new HashSet<>();
        }

        Set<Long> baseKeys = new HashSet<>();

        for (RecipeSelectionDTO s : selections) {
            Long baseKey = resolveBaseKey(s);
            if (baseKey != null) baseKeys.add(baseKey);
        }

        return baseKeys;
    }

    private Long resolveBaseKey(RecipeSelectionDTO selection) {
        if (selection == null) return null;

        if (selection.type() == RecipeSelectionDTO.RecipeSelectionType.RECIPE) {
            return selection.id();
        }

        if (selection.type() == RecipeSelectionDTO.RecipeSelectionType.TRANSFORMED_RECIPE) {
            return resolveBaseKeyForTransformed(selection.id());
        }

        return null;
    }

    private Long resolveBaseKeyForTransformed(Long transformedRecipeId) {
        if (transformedRecipeId == null) return null;

        return transformedRecipeRepository.findById(transformedRecipeId)
                .map(this::resolveBaseKeyForTransformedEntity)
                .orElse(null);
    }

    private Long resolveBaseKeyForTransformedEntity(TransformedRecipe tr) {
        if (tr == null) return null;
        if (tr.getBaseRecipe() == null) return null;
        return tr.getBaseRecipe().getId();
    }

    /**
     * usableForMealPlan 판단
     * - in-memory로 가족 알레르기 키워드를 재료 문자열에 매칭
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
        String lowered = s.toLowerCase(java.util.Locale.ROOT);
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
