package com.urisik.backend.domain.mealplan.ai.validation;

import com.urisik.backend.domain.mealplan.dto.common.RecipeSelectionDTO;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
import com.urisik.backend.domain.mealplan.exception.MealPlanException;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanErrorCode;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
public class MealPlanGenerationValidator {

    public void validateRecipeIdAssignments(
            List<MealPlan.SlotKey> selectedSlots,
            Map<MealPlan.SlotKey, RecipeSelectionDTO> recipeAssignments,
            List<RecipeSelectionDTO> candidateSelections
    ) {
        validateRecipeSelections(selectedSlots, recipeAssignments, candidateSelections);
    }

    public void validateRecipeAssignments(
            List<MealPlan.SlotKey> selectedSlots,
            Map<MealPlan.SlotKey, Long> recipeAssignments,
            List<Long> candidateRecipeIds
    ) {
        List<RecipeSelectionDTO> candidateSelections = (candidateRecipeIds == null)
                ? List.<RecipeSelectionDTO>of()
                : candidateRecipeIds.stream()
                .filter(Objects::nonNull)
                // RECIPE: baseRecipeId == recipeId
                .map(id -> new RecipeSelectionDTO(RecipeSelectionDTO.RecipeSelectionType.RECIPE, id, id))
                .toList();

        Map<MealPlan.SlotKey, RecipeSelectionDTO> assignments = new HashMap<>();
        if (recipeAssignments != null) {
            for (Map.Entry<MealPlan.SlotKey, Long> e : recipeAssignments.entrySet()) {
                if (e == null) continue;
                MealPlan.SlotKey k = e.getKey();
                Long id = e.getValue();
                assignments.put(k, id == null ? null : new RecipeSelectionDTO(RecipeSelectionDTO.RecipeSelectionType.RECIPE, id, id));
            }
        }

        validateRecipeSelections(selectedSlots, assignments, candidateSelections);
    }

    public void validateRecipeSelections(
            List<MealPlan.SlotKey> selectedSlots,
            Map<MealPlan.SlotKey, RecipeSelectionDTO> recipeAssignments,
            List<RecipeSelectionDTO> candidateSelections
    ) {
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }
        if (selectedSlots.stream().anyMatch(Objects::isNull)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }
        if (candidateSelections == null || candidateSelections.isEmpty() || candidateSelections.stream().anyMatch(Objects::isNull)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }
        if (recipeAssignments == null || recipeAssignments.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        // Reject extra keys that were not requested
        Set<MealPlan.SlotKey> requestedSet = new HashSet<>(selectedSlots);
        if (!recipeAssignments.keySet().equals(requestedSet)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        for (MealPlan.SlotKey slot : selectedSlots) {
            if (!recipeAssignments.containsKey(slot)) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
            RecipeSelectionDTO selection = recipeAssignments.get(slot);
            if (selection == null || selection.type() == null || selection.id() == null || selection.baseRecipeId() == null) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
        }

        // Candidate set check (type+id)
        Set<String> candidateSet = new HashSet<>();
        for (RecipeSelectionDTO c : candidateSelections) {
            if (c == null || c.type() == null || c.id() == null || c.baseRecipeId() == null) continue;
            candidateSet.add(candidateKey(c.type(), c.id()));
        }
        if (candidateSet.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        for (RecipeSelectionDTO selection : recipeAssignments.values()) {
            if (selection == null || selection.type() == null || selection.id() == null || selection.baseRecipeId() == null) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
            if (!candidateSet.contains(candidateKey(selection.type(), selection.id()))) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
        }
    }

    public void validateNoDuplicateBaseRecipe(
            Map<MealPlan.SlotKey, RecipeSelectionDTO> recipeAssignments,
            Function<RecipeSelectionDTO, Long> baseKeyResolver
    ) {
        if (recipeAssignments == null || recipeAssignments.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }
        if (baseKeyResolver == null) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        Set<Long> seen = new HashSet<>();
        for (RecipeSelectionDTO sel : recipeAssignments.values()) {
            if (sel == null) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
            Long baseKey = baseKeyResolver.apply(sel);
            if (baseKey == null) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
            if (!seen.add(baseKey)) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
        }
    }

    private static String candidateKey(RecipeSelectionDTO.RecipeSelectionType type, Long id) {
        return type.name() + ":" + id;
    }
}
