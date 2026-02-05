package com.urisik.backend.domain.mealplan.service;

import com.urisik.backend.domain.familyroom.service.FamilyRoomService;
import com.urisik.backend.domain.mealplan.dto.res.GetMealPlanResDTO;
import com.urisik.backend.domain.mealplan.entity.MealPlan;
import com.urisik.backend.domain.mealplan.enums.MealPlanStatus;
import com.urisik.backend.domain.mealplan.enums.MealType;
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
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class MealPlanQueryService {

    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;
    private final TransformedRecipeRepository transformedRecipeRepository;
    private final FamilyRoomService familyRoomService;

    /** 오늘의 식단 조회 */
    @Transactional(readOnly = true)
    public GetMealPlanResDTO.TodayMealPlanResDTO getTodayMealPlan(Long memberId, Long familyRoomId) {
        familyRoomService.validateMember(memberId, familyRoomId);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = normalizeToMonday(today);

        MealPlan mealPlan = mealPlanRepository.findByFamilyRoomIdAndWeekStartDate(familyRoomId, weekStart)
                .orElseThrow(() -> new MealPlanException(MealPlanErrorCode.MEAL_PLAN_NOT_FOUND));

        if (mealPlan.getStatus() != MealPlanStatus.CONFIRMED) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_NOT_CONFIRMED);
        }

        DayOfWeek dow = today.getDayOfWeek();
        List<MealType> mealTypes = List.of(MealType.LUNCH, MealType.DINNER);

        // mealType -> storedId (slot에 저장된 값: recipeId or transformedRecipeId)
        Map<MealType, Long> storedIdsByMealType = new LinkedHashMap<>();
        for (MealType mt : mealTypes) {
            MealPlan.SlotKey key = new MealPlan.SlotKey(mt, dow);
            Long storedId = mealPlan.getSlotValue(key);
            if (storedId != null) storedIdsByMealType.put(mt, storedId);
        }

        if (storedIdsByMealType.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_TODAY_EMPTY);
        }

        ResolvedMaps resolved = resolveStoredIds(familyRoomId, storedIdsByMealType.values());

        List<GetMealPlanResDTO.TodayMealDTO> meals = storedIdsByMealType.entrySet().stream()
                .map(e -> {
                    Long storedId = e.getValue();
                    ResolvedRecipe rr = resolved.byStoredId().get(storedId);
                    if (rr == null) {
                        throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_RECIPE_NOT_FOUND);
                    }
                    return new GetMealPlanResDTO.TodayMealDTO(
                            e.getKey(),
                            rr.recipeId(),
                            rr.transformedRecipeId(),
                            rr.title(),
                            null,
                            rr.ingredients(),
                            rr.instructions()
                    );
                })
                .toList();

        return new GetMealPlanResDTO.TodayMealPlanResDTO(
                today,
                mealPlan.getId(),
                mealPlan.getWeekStartDate(),
                meals
        );
    }

    /** 이번주 식단 조회 */
    @Transactional(readOnly = true)
    public GetMealPlanResDTO.WeeklyMealPlanResDTO getThisWeekMealPlan(Long memberId, Long familyRoomId, LocalDate anyDateInWeek) {
        familyRoomService.validateMember(memberId, familyRoomId);

        LocalDate weekStart = normalizeToMonday(anyDateInWeek == null ? LocalDate.now() : anyDateInWeek);

        MealPlan mealPlan = mealPlanRepository.findByFamilyRoomIdAndWeekStartDate(familyRoomId, weekStart)
                .orElseThrow(() -> new MealPlanException(MealPlanErrorCode.MEAL_PLAN_NOT_FOUND));

        if (mealPlan.getStatus() != MealPlanStatus.CONFIRMED) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_NOT_CONFIRMED);
        }

        Map<MealPlan.SlotKey, Long> snapshot = mealPlan.snapshotAllSlots();
        if (snapshot == null || snapshot.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_WEEKLY_EMPTY);
        }

        List<Long> storedIds = snapshot.values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        ResolvedMaps resolved = resolveStoredIds(familyRoomId, storedIds);

        Map<String, GetMealPlanResDTO.SlotSummaryDTO> slots = new LinkedHashMap<>();

        // 응답 슬롯 순서 고정
        // 정렬 기준: dayOfWeek(월->일) 오름차순, mealType (LUNCH -> DINNER) 순서로 고정
        List<Map.Entry<MealPlan.SlotKey, Long>> ordered = snapshot.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .sorted(Comparator
                        .comparing((Map.Entry<MealPlan.SlotKey, Long> e) -> e.getKey().dayOfWeek().getValue())
                        .thenComparing(e -> mealTypeOrder(e.getKey().mealType()))
                )
                .toList();

        for (Map.Entry<MealPlan.SlotKey, Long> e : ordered) {
            MealPlan.SlotKey key = e.getKey();
            Long storedId = e.getValue();

            ResolvedRecipe rr = resolved.byStoredId().get(storedId);
            if (rr == null) continue;

            String k = key.mealType().name() + "-" + key.dayOfWeek().name();
            slots.put(k, new GetMealPlanResDTO.SlotSummaryDTO(
                    rr.recipeId(),
                    rr.transformedRecipeId(),
                    rr.title(),
                    null,
                    summarizeDescription(rr.instructions()),
                    rr.ingredients()
            ));
        }

        return new GetMealPlanResDTO.WeeklyMealPlanResDTO(mealPlan.getId(), mealPlan.getWeekStartDate(), slots);
    }

    /** 최근 1개월 식단 조회 */
    @Transactional(readOnly = true)
    public GetMealPlanResDTO.MonthlyMealPlanResDTO getLastMonthMealPlan(Long memberId, Long familyRoomId) {
        familyRoomService.validateMember(memberId, familyRoomId);

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(1);

        LocalDate startWeek = normalizeToMonday(from);
        LocalDate endWeek = normalizeToMonday(to);

        List<MealPlan> plans = mealPlanRepository
                .findAllByFamilyRoomIdAndWeekStartDateBetweenOrderByWeekStartDateDesc(
                        familyRoomId, startWeek, endWeek
                )
                .stream()
                .filter(mp -> mp.getStatus() == MealPlanStatus.CONFIRMED)
                .toList();

        if (plans.isEmpty()) {
            return new GetMealPlanResDTO.MonthlyMealPlanResDTO(from, to, List.of());
        }

        // storedId 전체 수집
        Set<Long> allStoredIds = new HashSet<>();
        for (MealPlan mp : plans) {
            Map<MealPlan.SlotKey, Long> snapshot = mp.snapshotAllSlots();
            if (snapshot == null) continue;
            snapshot.values().stream().filter(Objects::nonNull).forEach(allStoredIds::add);
        }

        ResolvedMaps resolved = resolveStoredIds(familyRoomId, allStoredIds);

        List<GetMealPlanResDTO.WeekHistoryDTO> weeks = new ArrayList<>();
        for (MealPlan mp : plans) {
            Map<MealPlan.SlotKey, Long> snapshot = mp.snapshotAllSlots();
            if (snapshot == null) continue;

            Map<DayOfWeek, List<GetMealPlanResDTO.HistoryMealDTO>> dayMap = new EnumMap<>(DayOfWeek.class);

            for (Map.Entry<MealPlan.SlotKey, Long> e : snapshot.entrySet()) {
                MealPlan.SlotKey key = e.getKey();
                Long storedId = e.getValue();
                if (key == null || storedId == null) continue;

                ResolvedRecipe rr = resolved.byStoredId().get(storedId);
                if (rr == null) continue;

                dayMap.computeIfAbsent(key.dayOfWeek(), d -> new ArrayList<>())
                        .add(new GetMealPlanResDTO.HistoryMealDTO(
                                key.mealType(),
                                rr.recipeId(),
                                rr.transformedRecipeId(),
                                rr.title(),
                                null,
                                summarizeDescription(rr.instructions()),
                                rr.ingredients()
                        ));
            }

            // 각 요일 내 식사 순서 고정 (mealType 기준)
            dayMap.values().forEach(list ->
                    list.sort(Comparator.comparing(m -> mealTypeOrder(m.mealType())))
            );

            List<GetMealPlanResDTO.DayHistoryDTO> days = dayMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(en -> new GetMealPlanResDTO.DayHistoryDTO(en.getKey(), en.getValue()))
                    .toList();

            weeks.add(new GetMealPlanResDTO.WeekHistoryDTO(mp.getId(), mp.getWeekStartDate(), days));
        }

        return new GetMealPlanResDTO.MonthlyMealPlanResDTO(from, to, weeks);
    }

    // ----------------- resolve helpers -----------------

    private record ResolvedRecipe(
            Long recipeId,
            Long transformedRecipeId,
            String title,
            String ingredients,
            String instructions
    ) {}

    private record ResolvedMaps(
            Map<Long, ResolvedRecipe> byStoredId
    ) {}

    /** recipeId/transformedRecipeId/title/ingredients/instructions */
    private ResolvedMaps resolveStoredIds(Long familyRoomId, Collection<Long> storedIds) {
        if (storedIds == null) return new ResolvedMaps(Map.of());

        List<Long> ids = storedIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return new ResolvedMaps(Map.of());

        // storedId를 transformed_recipe.id로 먼저 조회
        Map<Long, TransformedRecipe> trById = transformedRecipeRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(TransformedRecipe::getId, tr -> tr, (a, b) -> a));

        // 나머지는 recipe.id로 취급
        List<Long> recipeIdsStored = ids.stream()
                .filter(id -> !trById.containsKey(id))
                .toList();

        // recipe 조회(변환본이 가리키는 recipe 포함)
        Set<Long> allRecipeIdsToLoad = new HashSet<>(recipeIdsStored);
        trById.values().stream()
                .map(tr -> tr.getBaseRecipe() == null ? null : tr.getBaseRecipe().getId())
                .filter(Objects::nonNull)
                .forEach(allRecipeIdsToLoad::add);

        Map<Long, Recipe> recipeById = recipeRepository.findAllById(allRecipeIdsToLoad)
                .stream()
                .collect(Collectors.toMap(Recipe::getId, r -> r, (a, b) -> a));

        // recipeId로 저장된 케이스는 transformedRecipeId도 내려주기 위해 조회
        Map<Long, Long> transformedIdByRecipeId = new HashMap<>();
        if (!recipeIdsStored.isEmpty()) {
            transformedRecipeRepository.findByFamilyRoomIdAndBaseRecipe_IdIn(familyRoomId, recipeIdsStored)
                    .stream()
                    .filter(tr -> tr.getBaseRecipe() != null)
                    .forEach(tr -> transformedIdByRecipeId.put(tr.getBaseRecipe().getId(), tr.getId()));
        }

        Map<Long, ResolvedRecipe> resolved = new HashMap<>();

        // storedId == transformedRecipeId 케이스
        for (Map.Entry<Long, TransformedRecipe> en : trById.entrySet()) {
            Long storedId = en.getKey();
            TransformedRecipe tr = en.getValue();

            Recipe base = (tr.getBaseRecipe() == null) ? null : recipeById.get(tr.getBaseRecipe().getId());
            if (base == null) continue;

            resolved.put(storedId, new ResolvedRecipe(
                    base.getId(),
                    tr.getId(),
                    base.getTitle(),
                    pickIngredients(tr, base),
                    pickInstructions(tr, base)
            ));
        }

        // storedId == recipeId 케이스
        for (Long storedId : recipeIdsStored) {
            Recipe r = recipeById.get(storedId);
            if (r == null) continue;

            resolved.put(storedId, new ResolvedRecipe(
                    r.getId(),
                    transformedIdByRecipeId.get(r.getId()),
                    r.getTitle(),
                    r.getIngredientsRaw(),
                    r.getInstructionsRaw()
            ));
        }

        return new ResolvedMaps(resolved);
    }

    private String pickIngredients(TransformedRecipe tr, Recipe base) {
        String v = tr.getInstructionsRaw();
        return (v == null || v.isBlank()) ? base.getIngredientsRaw() : v;
    }

    private String pickInstructions(TransformedRecipe tr, Recipe base) {
        String v = tr.getInstructionsRaw();
        return (v == null || v.isBlank()) ? base.getInstructionsRaw() : v;
    }

    /** 카드용 한 줄 설명: 첫 줄 우선, 없으면 앞 60자 */
    private String summarizeDescription(String instructions) {
        if (instructions == null) return "";
        String trimmed = instructions.trim();
        if (trimmed.isEmpty()) return "";

        int nl = trimmed.indexOf('\n');
        String firstLine = (nl >= 0 ? trimmed.substring(0, nl) : trimmed).trim();
        if (!firstLine.isEmpty()) {
            return firstLine.length() <= 60 ? firstLine : firstLine.substring(0, 60);
        }
        return trimmed.length() <= 60 ? trimmed : trimmed.substring(0, 60);
    }

    /** MealType 정렬 순서 지정 (LUNCH -> DINNER) */
    private static int mealTypeOrder(MealType mealType) {
        if (mealType == null) return Integer.MAX_VALUE;
        return switch (mealType) {
            case LUNCH -> 0;
            case DINNER -> 1;
        };
    }

    private static LocalDate normalizeToMonday(LocalDate date) {
        if (date == null) return normalizeToMonday(LocalDate.now());
        int diff = date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        return date.minusDays(diff);
    }
}
