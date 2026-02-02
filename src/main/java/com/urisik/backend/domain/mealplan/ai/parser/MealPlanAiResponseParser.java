
package com.urisik.backend.domain.mealplan.ai.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
     * 요구사항(안전성):
     * - JSON object 1개만 허용
     * - key는 selectedSlots에 포함된 슬롯만 허용 (추가 키 금지)
     * - selectedSlots의 모든 key가 반드시 존재해야 함 (누락 금지)
     * - value는 candidateRecipeIds 중 하나여야 함
     */
    public Map<MealPlan.SlotKey, Long> parse(
            String json,
            List<MealPlan.SlotKey> selectedSlots,
            List<Long> candidateRecipeIds
    ) {
        try {
            if (json == null || json.isBlank()) {
                throw fail();
            }
            if (selectedSlots == null || selectedSlots.isEmpty() || selectedSlots.stream().anyMatch(Objects::isNull)) {
                throw fail();
            }
            if (candidateRecipeIds == null || candidateRecipeIds.isEmpty() || candidateRecipeIds.stream().anyMatch(Objects::isNull)) {
                throw fail();
            }

            Map<String, Long> raw = objectMapper.readValue(json, new TypeReference<>() {});

            // Build allowed key set from selectedSlots (canonical: MEALTYPE_DAYOFWEEK)
            Set<String> allowedKeys = new HashSet<>();
            for (MealPlan.SlotKey sk : selectedSlots) {
                allowedKeys.add(toCanonicalKey(sk));
            }

            // Reject duplicate canonical keys (e.g., LUNCH_MONDAY and MONDAY_LUNCH)
            Set<String> seenCanonicals = new HashSet<>();

            // Reject unknown keys and convert
            Map<MealPlan.SlotKey, Long> result = new HashMap<>();
            for (Map.Entry<String, Long> entry : raw.entrySet()) {
                String rawKey = entry.getKey();
                Long recipeId = entry.getValue();

                MealPlan.SlotKey slotKey = parseSlotKey(rawKey);
                String canonical = toCanonicalKey(slotKey);

                // Reject duplicate canonical keys (e.g., LUNCH_MONDAY and MONDAY_LUNCH)
                if (!seenCanonicals.add(canonical)) {
                    throw fail();
                }

                if (!allowedKeys.contains(canonical)) {
                    throw fail();
                }
                if (recipeId == null) {
                    throw fail();
                }

                result.put(slotKey, recipeId);
            }

            // Ensure no missing keys
            for (String required : allowedKeys) {
                boolean present = result.keySet().stream().anyMatch(k -> toCanonicalKey(k).equals(required));
                if (!present) {
                    throw fail();
                }
            }

            // Ensure values are from candidateRecipeIds
            Set<Long> candidateSet = new HashSet<>(candidateRecipeIds);
            for (Map.Entry<MealPlan.SlotKey, Long> e : result.entrySet()) {
                if (!candidateSet.contains(e.getValue())) {
                    throw fail();
                }
            }

            return result;
        } catch (MealPlanException e) {
            throw e;
        } catch (Exception e) {
            // Any unexpected parsing exception is treated as parse failure
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

    private MealPlanException fail() {
        return new MealPlanException(MealPlanErrorCode.MEAL_PLAN_AI_RESPONSE_PARSE_FAILED);
    }
}
