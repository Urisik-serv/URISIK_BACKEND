package com.urisik.backend.domain.mealplan.service;

import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.familyroom.repository.FamilyRoomRepository;
import com.urisik.backend.domain.familyroom.service.FamilyRoomService;
import com.urisik.backend.domain.mealplan.ai.candidate.MealPlanCandidateProvider;
import com.urisik.backend.domain.mealplan.ai.generator.MealPlanGenerator;
import com.urisik.backend.domain.mealplan.ai.validation.MealPlanGenerationValidator;
import com.urisik.backend.domain.mealplan.dto.common.RecipeDTO;
import com.urisik.backend.domain.mealplan.dto.req.CreateMealPlanReqDTO;
import com.urisik.backend.domain.mealplan.dto.req.CreateMealPlanReqDTO.SlotRequest;
import com.urisik.backend.domain.mealplan.dto.req.RecipeSelectionDTO;
import com.urisik.backend.domain.mealplan.dto.req.UpdateMealPlanReqDTO;
import com.urisik.backend.domain.mealplan.dto.res.CreateMealPlanResDTO;
import com.urisik.backend.domain.mealplan.dto.res.UpdateMealPlanResDTO;
import com.urisik.backend.domain.mealplan.dto.res.ConfirmMealPlanResDTO;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
import com.urisik.backend.domain.mealplan.enums.MealPlanStatus;
import com.urisik.backend.domain.mealplan.exception.MealPlanException;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanErrorCode;
import com.urisik.backend.domain.mealplan.repository.MealPlanRepository;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final FamilyRoomRepository familyRoomRepository;
    private final FamilyRoomService familyRoomService;

    private final RecipeRepository recipeRepository;
    private final TransformedRecipeRepository transformedRecipeRepository;

    private final MealPlanCandidateProvider candidateProvider;
    private final MealPlanGenerator generator;
    private final MealPlanGenerationValidator validator;

    /**
     * 식단 생성 API
     */
    @Transactional
    public CreateMealPlanResDTO createMealPlan(Long memberId, Long familyRoomId, CreateMealPlanReqDTO req) {
        LocalDate weekStart = normalizeToMonday(req.weekStartDate());

        // 방장 검증
        familyRoomService.validateLeader(memberId, familyRoomId);

        Optional<MealPlan> existingMealPlanOpt =
                mealPlanRepository.findByFamilyRoomIdAndWeekStartDate(familyRoomId, weekStart);

        MealPlan mealPlan;
        if (existingMealPlanOpt.isPresent()) {
            mealPlan = existingMealPlanOpt.get();

            // 확정된 식단은 재생성/수정 불가
            if (mealPlan.getStatus() == MealPlanStatus.CONFIRMED) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_ALREADY_CONFIRMED);
            }

            // DRAFT 식단이 있을 시 regenerate=false면 중복 생성 불가
            if (!req.regenerate()) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_ALREADY_EXISTS);
            }

        } else {
            FamilyRoom familyRoom = familyRoomRepository.findById(familyRoomId)
                    .orElseThrow(() -> new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED));

            mealPlan = MealPlan.draft(familyRoom, weekStart);
        }

        List<MealPlan.SlotKey> selectedSlots = distinctPreserveOrder(toSlotKeys(req.selectedSlots()));
        if (selectedSlots.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        GenerationResult generationResult = generateForSelectedSlots(familyRoomId, selectedSlots);

        // 저장 (transformed 있으면 transformedId, 없으면 recipeId 저장)
        mealPlan.applySelectedSlots(generationResult.chosenAssignments());
        mealPlanRepository.save(mealPlan);

        // 응답: chosenId + title
        Map<Long, String> recipeTitles = loadRecipeTitles(generationResult.recipeAssignments().values());

        Map<String, RecipeDTO> slots = buildSlotResponse(
                selectedSlots,
                generationResult.recipeAssignments(),
                generationResult.chosenAssignments(),
                recipeTitles
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
            Map<MealPlan.SlotKey, Long> chosenAssignments
    ) {
    }

    private GenerationResult generateForSelectedSlots(
            Long familyRoomId,
            List<MealPlan.SlotKey> selectedSlots
    ) {
        // 후보군 조회
        List<Long> candidateRecipeIdsRaw = candidateProvider.getCandidateRecipeIds(familyRoomId);
        if (candidateRecipeIdsRaw == null || candidateRecipeIdsRaw.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED);
        }

        // null 방어만 수행 (중복 허용)
        List<Long> candidateRecipeIds = candidateRecipeIdsRaw.stream()
                .filter(Objects::nonNull)
                .toList();

        if (candidateRecipeIds.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        // AI 생성: slot -> recipeId
        Map<MealPlan.SlotKey, Long> recipeAssignments;
        try {
            recipeAssignments =
                    generator.generateRecipeAssignments(selectedSlots, candidateRecipeIds);
        } catch (Exception e) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_GENERATION_FAILED);
        }

        // 검증 (slot 누락 / 후보군 외 id 등)
        validator.validateRecipeAssignments(
                selectedSlots,
                recipeAssignments,
                candidateRecipeIds
        );

        // transformed 있으면 transformedId, 없으면 recipeId 그대로
        Map<MealPlan.SlotKey, Long> chosenAssignments = new HashMap<>();
        for (MealPlan.SlotKey slot : selectedSlots) {
            Long recipeId = recipeAssignments.get(slot);
            if (recipeId == null) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }

            Long chosenId = transformedRecipeRepository
                    .findByRecipe_IdAndFamilyRoomId(recipeId, familyRoomId)
                    .map(TransformedRecipe::getId)
                    .orElse(recipeId);

            chosenAssignments.put(slot, chosenId);
        }

        return new GenerationResult(recipeAssignments, chosenAssignments);
    }

    private Map<String, RecipeDTO> buildSlotResponse(
            List<MealPlan.SlotKey> selectedSlots,
            Map<MealPlan.SlotKey, Long> recipeAssignments,
            Map<MealPlan.SlotKey, Long> chosenAssignments,
            Map<Long, String> recipeTitles
    ) {
        Map<String, RecipeDTO> slots = new LinkedHashMap<>();
        for (MealPlan.SlotKey slot : selectedSlots) {
            Long recipeId = recipeAssignments.get(slot);
            Long chosenId = chosenAssignments.get(slot);

            String title = recipeTitles.getOrDefault(recipeId, "UNKNOWN");
            String key = slot.mealType().name() + "-" + slot.dayOfWeek().name();

            slots.put(key, new RecipeDTO(chosenId, title));
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

    private static List<MealPlan.SlotKey> toSlotKeys(List<SlotRequest> requests) {
        if (requests == null) {
            return List.of();
        }

        List<MealPlan.SlotKey> keys = new ArrayList<>();
        for (SlotRequest r : requests) {
            if (r == null || r.mealType() == null || r.dayOfWeek() == null) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
            keys.add(new MealPlan.SlotKey(r.mealType(), r.dayOfWeek()));
        }
        return keys;
    }

    private static List<MealPlan.SlotKey> distinctPreserveOrder(List<MealPlan.SlotKey> slots) {
        if (slots == null) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(slots));
    }

    /**
     * 식단 수정 API
     */
    @Transactional
    public UpdateMealPlanResDTO updateMealPlan(
            Long memberId,
            Long familyRoomId,
            Long mealPlanId,
            UpdateMealPlanReqDTO req
    ) {
        // 방장 검증
        familyRoomService.validateLeader(memberId, familyRoomId);

        MealPlan mealPlan = mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new MealPlanException(MealPlanErrorCode.MEAL_PLAN_NOT_FOUND));

        // 가족방 일치 검증
        if (!mealPlan.getFamilyRoom().getId().equals(familyRoomId)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_NOT_IN_FAMILY_ROOM);
        }

        // 상태 검증: DRAFT만 수정 가능
        if (mealPlan.getStatus() != MealPlanStatus.DRAFT) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_NOT_DRAFT);
        }

        // slot 파싱
        MealPlan.SlotKey slotKey = new MealPlan.SlotKey(
                req.slot().mealType(),
                req.slot().dayOfWeek()
        );

        // 선택 슬롯인지 검증
        if (!mealPlan.isSelectedSlot(slotKey)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_SLOT_NOT_SELECTED);
        }

        RecipeSelectionDTO selection = req.selectedRecipe();
        if (selection == null || selection.id() == null || selection.type() == null) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        // 선택 타입에 따라 저장할 id 결정 + title 결정
        Long chosenId;
        String title;

        switch (selection.type()) {
            case RECIPE -> {
                Recipe recipe = recipeRepository.findById(selection.id())
                        .orElseThrow(() ->
                                new MealPlanException(MealPlanErrorCode.MEAL_PLAN_RECIPE_NOT_FOUND)
                        );
                chosenId = recipe.getId();
                title = recipe.getTitle();
            }

            case TRANSFORMED_RECIPE -> {
                TransformedRecipe tr = transformedRecipeRepository.findById(selection.id())
                        .orElseThrow(() ->
                                new MealPlanException(MealPlanErrorCode.MEAL_PLAN_TRANSFORMED_RECIPE_NOT_FOUND)
                        );
                chosenId = tr.getId();
                title = tr.getRecipe() == null ? "UNKNOWN" : tr.getRecipe().getTitle();
            }

            default -> throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        // 실제 슬롯 컬럼 업데이트
        mealPlan.updateSlot(slotKey, chosenId);
        mealPlanRepository.save(mealPlan);

        String updatedSlotKeyStr = slotKey.mealType().name() + "-" + slotKey.dayOfWeek().name();

        return new UpdateMealPlanResDTO(
                mealPlan.getId(),
                mealPlan.getStatus(),
                updatedSlotKeyStr,
                new RecipeDTO(chosenId, title)
        );
    }

    private Map<Long, String> loadRecipeTitles(Collection<Long> recipeIds) {
        if (recipeIds == null || recipeIds.isEmpty()) {
            return Map.of();
        }

        // null 방어 + 중복 제거
        List<Long> ids = recipeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (ids.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> titles = new HashMap<>();
        recipeRepository.findAllById(ids)
                .forEach(r -> titles.put(r.getId(), r.getTitle()));

        return titles;
    }

    /**
     * 식단 확정 API
     * - DRAFT 상태의 식단을 CONFIRMED로 전환
     * - 확정 시 가족방 식단 생성 횟수 +1
     */
    @Transactional
    public ConfirmMealPlanResDTO confirmMealPlan(Long memberId, Long familyRoomId, Long mealPlanId) {
        // 방장 검증
        familyRoomService.validateLeader(memberId, familyRoomId);

        MealPlan mealPlan = mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new MealPlanException(MealPlanErrorCode.MEAL_PLAN_NOT_FOUND));

        // 가족방 일치 검증
        if (!mealPlan.getFamilyRoom().getId().equals(familyRoomId)) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_NOT_IN_FAMILY_ROOM);
        }

        // 상태 검증
        // - 이미 확정된 식단은 재확정 불가 (명확한 에러코드 반환)
        if (mealPlan.getStatus() == MealPlanStatus.CONFIRMED) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_ALREADY_CONFIRMED);
        }

        // - DRAFT만 확정 가능
        if (mealPlan.getStatus() != MealPlanStatus.DRAFT) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_NOT_DRAFT);
        }

        // 선택된 슬롯이 모두 채워져 있어야 확정 가능
        validateAllSelectedSlotsFilled(mealPlan);

        // 상태 변경 + 가족방 생성횟수 증가
        mealPlan.updateStatus(MealPlanStatus.CONFIRMED);
        mealPlan.getFamilyRoom().incrementMealPlanGenerationCount();

        mealPlanRepository.save(mealPlan);

        FamilyRoom familyRoom = mealPlan.getFamilyRoom();
        return new ConfirmMealPlanResDTO(
                mealPlan.getId(),
                mealPlan.getStatus(),
                mealPlan.getWeekStartDate(),
                familyRoom.getMealPlanGenerationCount()
        );
    }

    private void validateAllSelectedSlotsFilled(MealPlan mealPlan) {
        Collection<MealPlan.SlotKey> selectedSlots = mealPlan.getSelectedSlots();

        if (selectedSlots == null || selectedSlots.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
        }

        for (MealPlan.SlotKey key : selectedSlots) {
            if (key == null) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }

            Long value = mealPlan.getSlotValue(key);
            if (value == null) {
                throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_VALIDATION_FAILED);
            }
        }
    }
}
