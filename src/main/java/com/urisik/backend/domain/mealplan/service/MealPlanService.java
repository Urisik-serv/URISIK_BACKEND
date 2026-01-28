package com.urisik.backend.domain.mealplan.service;

import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.familyroom.repository.FamilyRoomRepository;
import com.urisik.backend.domain.familyroom.service.FamilyRoomService;
import com.urisik.backend.domain.mealplan.ai.candidate.MealPlanCandidateProvider;
import com.urisik.backend.domain.mealplan.ai.generator.MealPlanGenerator;
import com.urisik.backend.domain.mealplan.ai.resolver.TransformedRecipeResolver;
import com.urisik.backend.domain.mealplan.ai.validation.MealPlanGenerationValidator;
import com.urisik.backend.domain.mealplan.dto.common.RecipeDTO;
import com.urisik.backend.domain.mealplan.dto.req.CreateMealPlanReqDTO;
import com.urisik.backend.domain.mealplan.dto.res.CreateMealPlanResDTO;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
import com.urisik.backend.domain.mealplan.enums.MealPlanStatus;
import com.urisik.backend.domain.mealplan.exception.MealPlanException;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanErrorCode;
import com.urisik.backend.domain.mealplan.repository.MealPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final FamilyRoomRepository familyRoomRepository;
    private final FamilyRoomService familyRoomService;

    private final MealPlanCandidateProvider candidateProvider;
    private final MealPlanGenerator generator;
    private final MealPlanGenerationValidator validator;
    private final TransformedRecipeResolver transformedRecipeResolver;

    // TODO(리팩터링): recipe 테이블 연동되면 제거하고 RecipeRepository로 title 조회
    private static final Map<Long, String> DUMMY_RECIPE_TITLES = Map.of(
            1001L, "불고기 덮밥",
            1002L, "김치찌개",
            1003L, "된장찌개",
            1004L, "닭갈비",
            1005L, "비빔밥",
            1006L, "카레"
    );

    @Transactional
    public CreateMealPlanResDTO createMealPlan(Long memberId, Long familyRoomId, CreateMealPlanReqDTO req) {
        LocalDate weekStart = normalizeToMonday(req.weekStartDate());

        // 방장 검증 (생성/재생성 모두)
        familyRoomService.validateLeader(memberId, familyRoomId);

        Optional<MealPlan> existingMealPlanOpt =
                mealPlanRepository.findByFamilyRoomIdAndWeekStartDate(familyRoomId, weekStart);

        MealPlan mealPlan;
        if (existingMealPlanOpt.isPresent()) {
            mealPlan = existingMealPlanOpt.get();

            if (!req.regenerate()) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_ALREADY_EXISTS);
            }

            if (mealPlan.getStatus() == MealPlanStatus.CONFIRMED) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
        } else {
            FamilyRoom familyRoom = familyRoomRepository.findById(familyRoomId)
                    .orElseThrow(() -> new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED));

            mealPlan = MealPlan.draft(familyRoom, weekStart);
        }

        List<MealPlan.SlotKey> selectedSlots = distinctPreserveOrder(req.selectedSlots());
        if (selectedSlots.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        GenerationResult generationResult = generateForSelectedSlots(familyRoomId, selectedSlots);

        // 저장 (MealPlan 슬롯에 transformed_recipe.id 저장)
        mealPlan.applySelectedSlots(generationResult.transformedAssignments());
        mealPlanRepository.save(mealPlan);

        // 응답: transformedRecipeId + title (현재 더미)
        Map<String, RecipeDTO> slots = buildSlotResponse(
                selectedSlots,
                generationResult.recipeAssignments(),
                generationResult.transformedAssignments()
        );

        return new CreateMealPlanResDTO(
                mealPlan.getId(),
                familyRoomId,
                mealPlan.getWeekStartDate(),
                mealPlan.getStatus(),
                slots
        );
    }

    private record GenerationResult(
            Map<MealPlan.SlotKey, Long> recipeAssignments,
            Map<MealPlan.SlotKey, Long> transformedAssignments
    ) {
    }

    private GenerationResult generateForSelectedSlots(Long familyRoomId, List<MealPlan.SlotKey> selectedSlots) {
        // 후보군: 원본 recipeId 목록
        List<Long> candidateRecipeIds = candidateProvider.getCandidateRecipeIds(familyRoomId);
        if (candidateRecipeIds == null || candidateRecipeIds.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED);
        }

        // 생성: 슬롯 -> 원본 recipeId 배정
        Map<MealPlan.SlotKey, Long> recipeAssignments;
        try {
            recipeAssignments = generator.generateRecipeAssignments(selectedSlots, candidateRecipeIds);
        } catch (Exception e) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED);
        }

        // 검증
        validator.validateRecipeAssignments(selectedSlots, recipeAssignments, candidateRecipeIds);

        // resolve: recipeId -> transformedRecipeId (가족 기준)
        Map<MealPlan.SlotKey, Long> transformedAssignments = new HashMap<>();
        for (MealPlan.SlotKey slot : selectedSlots) {
            Long recipeId = recipeAssignments.get(slot);
            Long transformedRecipeId = transformedRecipeResolver.resolveOrCreate(familyRoomId, recipeId);
            transformedAssignments.put(slot, transformedRecipeId);
        }

        return new GenerationResult(recipeAssignments, transformedAssignments);
    }

    private Map<String, RecipeDTO> buildSlotResponse(
            List<MealPlan.SlotKey> selectedSlots,
            Map<MealPlan.SlotKey, Long> recipeAssignments,
            Map<MealPlan.SlotKey, Long> transformedAssignments
    ) {
        Map<String, RecipeDTO> slots = new LinkedHashMap<>();
        for (MealPlan.SlotKey slot : selectedSlots) {
            Long recipeId = recipeAssignments.get(slot);
            Long transformedRecipeId = transformedAssignments.get(slot);

            String title = DUMMY_RECIPE_TITLES.getOrDefault(recipeId, "UNKNOWN");
            String key = slot.mealType().name() + "-" + slot.dayOfWeek().name();

            slots.put(key, new RecipeDTO(transformedRecipeId, title));
        }
        return slots;
    }

    private static LocalDate normalizeToMonday(LocalDate date) {
        if (date == null) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }
        int diff = date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        return date.minusDays(diff);
    }

    private static List<MealPlan.SlotKey> distinctPreserveOrder(List<MealPlan.SlotKey> slots) {
        if (slots == null) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(slots));
    }
}
