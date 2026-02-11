package com.urisik.backend.domain.mealplan.ai.candidate;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import com.urisik.backend.domain.familyroom.dto.res.FamilyWishListItemResDTO;
import com.urisik.backend.domain.familyroom.repository.FamilyWishListExclusionRepository;
import com.urisik.backend.domain.familyroom.service.FamilyWishListQueryService;
import com.urisik.backend.domain.mealplan.dto.common.RecipeSelectionDTO;
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
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MealPlanCandidateProviderImpl implements MealPlanCandidateProvider {

    private final FamilyWishListQueryService familyWishListQueryService;
    private final TransformedRecipeRepository transformedRecipeRepository;
    private final RecipeRepository recipeRepository;
    private final MemberAllergyRepository memberAllergyRepository;
    private final FamilyWishListExclusionRepository familyWishListExclusionRepository;

    private static final int MAX_SLOTS = 14;
    private static final int TARGET_POOL_SIZE = MAX_SLOTS * 8; // 112
    private static final int DB_BATCH_SIZE = MAX_SLOTS * 5;    // 70
    private static final int DB_MAX_ATTEMPTS = 4;

    /** 가족 위시리스트 후보 제공 */
    @Override
    public List<RecipeSelectionDTO> getWishRecipeSelections(Long memberId, Long familyRoomId) {

        List<FamilyWishListItemResDTO> wishItems =
                familyWishListQueryService.getFamilyWishList(memberId, familyRoomId);

        if (wishItems == null || wishItems.isEmpty()) {
            return Collections.emptyList();
        }

        // Batch-load transformed recipes used in wishes to avoid N+1 lookups
        Set<Long> wishedTransformedIds = new HashSet<>();
        for (FamilyWishListItemResDTO item : wishItems) {
            if (isTransformed(item) && item.getId() != null) {
                wishedTransformedIds.add(item.getId());
            }
        }

        Map<Long, Long> baseIdByTransformedId = new HashMap<>();
        if (!wishedTransformedIds.isEmpty()) {
            List<TransformedRecipe> wishedTransformed = transformedRecipeRepository.findAllById(wishedTransformedIds);
            for (TransformedRecipe tr : wishedTransformed) {
                if (tr == null || tr.getId() == null) continue;
                Long baseId = resolveBaseKeyForTransformedEntity(tr);
                if (baseId != null) baseIdByTransformedId.put(tr.getId(), baseId);
            }
        }

        List<RecipeSelectionDTO> result = new ArrayList<>();
        Set<Long> usedBaseKeys = new HashSet<>();

        for (FamilyWishListItemResDTO item : wishItems) {
            if (item == null || item.getId() == null) continue;

            // transformed 우선
            if (isTransformed(item)) {
                Long transformedRecipeId = item.getId();
                Long baseKey = baseIdByTransformedId.get(transformedRecipeId);
                if (baseKey == null) continue;
                if (!usedBaseKeys.add(baseKey)) continue;

                result.add(new RecipeSelectionDTO(
                        RecipeSelectionDTO.RecipeSelectionType.TRANSFORMED_RECIPE,
                        transformedRecipeId,
                        baseKey
                ));
                continue;
            }

            if (isRecipe(item)) {
                Long recipeId = item.getId();
                Long baseKey = recipeId;
                if (!usedBaseKeys.add(baseKey)) continue;

                result.add(new RecipeSelectionDTO(
                        RecipeSelectionDTO.RecipeSelectionType.RECIPE,
                        recipeId,
                        recipeId
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

        // 1. 변형 레시피
        for (int attempt = 0; attempt < DB_MAX_ATTEMPTS && pool.size() < TARGET_POOL_SIZE; attempt++) {
            List<TransformedRecipeRepository.TransformedCandidateRow> rows =
                    transformedRecipeRepository.findRandomCandidateRows(
                            org.springframework.data.domain.PageRequest.of(0, DB_BATCH_SIZE)
                    );

            if (rows == null || rows.isEmpty()) break;

            for (TransformedRecipeRepository.TransformedCandidateRow row : rows) {
                if (row == null || row.getId() == null || row.getBaseRecipeId() == null) continue;

                Long transformedId = row.getId();
                Long baseKey = row.getBaseRecipeId();

                // 방장 exclusion(조회에서만 제외)도 후보군에서 제외
                if (excludedTransformedRecipeIds != null && excludedTransformedRecipeIds.contains(transformedId)) continue;

                // 알레르기 필터링: unsafe(알레르기 포함)면 제외
                if (!isUsableForMealPlan(row.getIngredientsRaw(), normalizedAllergens)) continue;

                // wish 후보에서 이미 사용된 baseKey는 fallback에서 제외
                if (excludedBaseKeys.contains(baseKey)) continue;

                pool.add(new RecipeSelectionDTO(
                        RecipeSelectionDTO.RecipeSelectionType.TRANSFORMED_RECIPE,
                        transformedId,
                        baseKey
                ));

                if (pool.size() >= TARGET_POOL_SIZE) break;
            }
        }

        // 2. 원형 레시피 (DB에서 제한/샘플링해서 가져오기: findAll() 금지)
        for (int attempt = 0; attempt < DB_MAX_ATTEMPTS && pool.size() < TARGET_POOL_SIZE; attempt++) {
            List<RecipeRepository.RecipeCandidateRow> rows =
                    recipeRepository.findRandomCandidateRows(org.springframework.data.domain.PageRequest.of(0, DB_BATCH_SIZE));

            if (rows == null || rows.isEmpty()) break;

            for (RecipeRepository.RecipeCandidateRow row : rows) {
                if (row == null || row.getId() == null) continue;

                Long recipeId = row.getId();
                if (excludedRecipeIds != null && excludedRecipeIds.contains(recipeId)) continue;

                // 알레르기 필터링: unsafe(알레르기 포함)면 제외
                if (!isUsableForMealPlan(row.getIngredientsRaw(), normalizedAllergens)) continue;

                Long baseKey = recipeId;
                if (excludedBaseKeys.contains(baseKey)) continue;

                pool.add(new RecipeSelectionDTO(
                        RecipeSelectionDTO.RecipeSelectionType.RECIPE,
                        recipeId,
                        recipeId
                ));

                if (pool.size() >= TARGET_POOL_SIZE) break;
            }
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
        // baseRecipeId is always set when constructing RecipeSelectionDTO (RECIPE: id, TRANSFORMED: baseRecipe.id)
        return selection.baseRecipeId();
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

        String[] parts = ingredientsRaw.split("[\\n\\r,;/\\t·•]+");

        List<String> tokens = new ArrayList<>();
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (!t.isEmpty()) tokens.add(t);
        }
        return tokens;
    }

    private boolean isTransformed(FamilyWishListItemResDTO item) {
        if (item == null || item.getType() == null) return false;
        return "TRANSFORMED_RECIPE".equalsIgnoreCase(item.getType().trim());
    }

    private boolean isRecipe(FamilyWishListItemResDTO item) {
        if (item == null || item.getType() == null) return false;
        return "RECIPE".equalsIgnoreCase(item.getType().trim());
    }
}
