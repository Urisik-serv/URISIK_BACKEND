
package com.urisik.backend.domain.mealplan.ai.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urisik.backend.domain.mealplan.dto.common.RecipeSelectionDTO;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
import com.urisik.backend.domain.mealplan.enums.MealType;
import com.urisik.backend.domain.mealplan.exception.MealPlanException;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanErrorCode;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public class MealPlanAiResponseParser {

    private final ObjectMapper objectMapper;

    public MealPlanAiResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * AI 응답(JSON)을 SlotKey -> recipeId 맵으로 변환
     */
    public Map<MealPlan.SlotKey, Long> parse(
            String json,
            List<MealPlan.SlotKey> selectedSlots,
            List<Long> candidateRecipeIds
    ) {
        Map<MealPlan.SlotKey, RecipeSelectionDTO> selections = parseSelections(json, selectedSlots, toCandidateSelections(candidateRecipeIds));
        Map<MealPlan.SlotKey, Long> result = new HashMap<>();
        for (Map.Entry<MealPlan.SlotKey, RecipeSelectionDTO> e : selections.entrySet()) {
            result.put(e.getKey(), e.getValue().id());
        }
        return result;
    }

    /**
     * AI 응답(JSON)을 SlotKey -> RecipeSelectionDTO 맵으로 변환
     * 요구사항:
     * - JSON object 1개만 허용
     * - key는 selectedSlots에 포함된 슬롯만 허용 (추가 키 금지)
     * - selectedSlots의 모든 key가 반드시 존재해야 함 (누락 금지)
     * - value는 {id, type} 형식이어야 하며, candidateSelections 중 하나여야 함
     */
    public Map<MealPlan.SlotKey, RecipeSelectionDTO> parseSelections(
            String json,
            List<MealPlan.SlotKey> selectedSlots,
            List<RecipeSelectionDTO> candidateSelections
    ) {
        try {
            if (json == null || json.isBlank()) {
                throw fail();
            }
            if (selectedSlots == null || selectedSlots.isEmpty() || selectedSlots.stream().anyMatch(Objects::isNull)) {
                throw fail();
            }
            if (candidateSelections == null || candidateSelections.isEmpty() || candidateSelections.stream().anyMatch(Objects::isNull)) {
                throw fail();
            }

            Map<String, AiValue> raw = objectMapper.readValue(json, new TypeReference<>() {});

            // Build allowed key set from selectedSlots (canonical: MEALTYPE_DAYOFWEEK)
            Set<String> allowedKeys = new HashSet<>();
            for (MealPlan.SlotKey sk : selectedSlots) {
                allowedKeys.add(toCanonicalKey(sk));
            }

            // Reject duplicate canonical keys (e.g., LUNCH_MONDAY and MONDAY_LUNCH)
            Set<String> seenCanonicals = new HashSet<>();

            // Candidate set for fast validation (type+id) + baseRecipeId lookup
            Set<String> candidateSet = new HashSet<>();
            Map<String, Long> baseIdByKey = new HashMap<>();
            for (RecipeSelectionDTO c : candidateSelections) {
                if (c == null || c.type() == null || c.id() == null || c.baseRecipeId() == null) continue;
                String key = candidateKey(c.type(), c.id());
                candidateSet.add(key);
                baseIdByKey.put(key, c.baseRecipeId());
            }
            if (candidateSet.isEmpty()) {
                throw fail();
            }

            Map<MealPlan.SlotKey, RecipeSelectionDTO> result = new HashMap<>();
            for (Map.Entry<String, AiValue> entry : raw.entrySet()) {
                String rawKey = entry.getKey();
                AiValue value = entry.getValue();

                MealPlan.SlotKey slotKey = parseSlotKey(rawKey);
                String canonical = toCanonicalKey(slotKey);

                if (!seenCanonicals.add(canonical)) {
                    throw fail();
                }

                if (!allowedKeys.contains(canonical)) {
                    throw fail();
                }
                if (value == null || value.id == null || value.type == null || value.type.isBlank()) {
                    throw fail();
                }

                RecipeSelectionDTO.RecipeSelectionType selectionType;
                try {
                    selectionType = RecipeSelectionDTO.RecipeSelectionType.valueOf(value.type.trim().toUpperCase());
                } catch (Exception e) {
                    throw fail();
                }

                Long baseId = baseIdByKey.get(candidateKey(selectionType, value.id));
                if (baseId == null) {
                    // For RECIPE, base can default to id. For TRANSFORMED, missing base is invalid.
                    if (selectionType == RecipeSelectionDTO.RecipeSelectionType.RECIPE) {
                        baseId = value.id;
                    } else {
                        throw fail();
                    }
                }

                RecipeSelectionDTO selection = new RecipeSelectionDTO(selectionType, value.id, baseId);

                if (!candidateSet.contains(candidateKey(selection.type(), selection.id()))) {
                    throw fail();
                }

                result.put(slotKey, selection);
            }

            // Ensure no missing keys
            for (String required : allowedKeys) {
                boolean present = result.keySet().stream().anyMatch(k -> toCanonicalKey(k).equals(required));
                if (!present) {
                    throw fail();
                }
            }

            return result;
        } catch (MealPlanException e) {
            throw e;
        } catch (Exception e) {
            throw fail();
        }
    }

    private String toCanonicalKey(MealPlan.SlotKey slotKey) {
        return slotKey.mealType().name() + "_" + slotKey.dayOfWeek().name();
    }

    /**
     * 허용 포맷:
     * - "LUNCH_MONDAY" / "DINNER_SUNDAY" (MealType_DayOfWeek)
     * - "MONDAY_LUNCH" / "SUNDAY_DINNER" (DayOfWeek_MealType)
     */
    private MealPlan.SlotKey parseSlotKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            throw fail();
        }

        String normalized = rawKey.trim().toUpperCase();
        String[] parts = normalized.split("_");
        if (parts.length != 2) {
            throw fail();
        }

        // Try MealType_DayOfWeek
        try {
            MealType mealType = MealType.valueOf(parts[0]);
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(parts[1]);
            return new MealPlan.SlotKey(mealType, dayOfWeek);
        } catch (Exception ignored) {
            // Try DayOfWeek_MealType
        }

        // Try DayOfWeek_MealType
        try {
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(parts[0]);
            MealType mealType = MealType.valueOf(parts[1]);
            return new MealPlan.SlotKey(mealType, dayOfWeek);
        } catch (Exception e) {
            throw fail();
        }
    }

    private static String candidateKey(RecipeSelectionDTO.RecipeSelectionType type, Long id) {
        return type.name() + ":" + id;
    }

    private List<RecipeSelectionDTO> toCandidateSelections(List<Long> candidateRecipeIds) {
        // Backward-compat: treat legacy candidates as canonical recipes
        if (candidateRecipeIds == null || candidateRecipeIds.isEmpty()) {
            return List.<RecipeSelectionDTO>of();
        }
        return candidateRecipeIds.stream()
                .filter(Objects::nonNull)
                // RECIPE: baseRecipeId == recipeId
                .map(id -> new RecipeSelectionDTO(RecipeSelectionDTO.RecipeSelectionType.RECIPE, id, id))
                .toList();
    }

    private record AiValue(Long id, String type) {}

    private MealPlanException fail() {
        return new MealPlanException(MealPlanErrorCode.MEAL_PLAN_AI_RESPONSE_PARSE_FAILED);
    }
}
