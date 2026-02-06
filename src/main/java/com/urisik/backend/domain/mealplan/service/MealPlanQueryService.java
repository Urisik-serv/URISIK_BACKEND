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

        // mealType -> storedRef (slot에 저장된 값: (type, id))
        Map<MealType, MealPlan.SlotRef> storedRefsByMealType = new LinkedHashMap<>();
        for (MealType mt : mealTypes) {
            MealPlan.SlotKey key = new MealPlan.SlotKey(mt, dow);
            MealPlan.SlotRef ref = mealPlan.getSlotRef(key);
            if (ref != null && ref.id() != null && ref.type() != null) {
                storedRefsByMealType.put(mt, ref);
            }
        }

        if (storedRefsByMealType.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_TODAY_EMPTY);
        }

        Set<SlotRefKey> refKeys = storedRefsByMealType.values().stream()
                .map(SlotRefKey::of)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        ResolvedMaps resolved = resolveStoredRefs(familyRoomId, refKeys);

        List<GetMealPlanResDTO.TodayMealDTO> meals = storedRefsByMealType.entrySet().stream()
                .map(e -> {
                    MealPlan.SlotRef ref = e.getValue();
                    SlotRefKey refKey = SlotRefKey.of(ref);
                    ResolvedRecipe rr = (refKey == null) ? null : resolved.byRef().get(refKey);
                    if (rr == null) {
                        throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_RECIPE_NOT_FOUND);
                    }
                    return new GetMealPlanResDTO.TodayMealDTO(
                            e.getKey(),
                            toDtoSlotRefType(ref.type()),
                            ref.id(),
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

        Map<MealPlan.SlotKey, MealPlan.SlotRef> snapshot = mealPlan.snapshotAllSlotRefs();
        if (snapshot == null || snapshot.isEmpty()) {
            throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_WEEKLY_EMPTY);
        }

        Set<SlotRefKey> refKeys = snapshot.values().stream()
                .map(SlotRefKey::of)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        ResolvedMaps resolved = resolveStoredRefs(familyRoomId, refKeys);

        Map<String, GetMealPlanResDTO.SlotSummaryDTO> slots = new LinkedHashMap<>();

        // 응답 슬롯 순서 고정
        // 정렬 기준: dayOfWeek(월->일) 오름차순, mealType (LUNCH -> DINNER) 순서로 고정
        List<Map.Entry<MealPlan.SlotKey, MealPlan.SlotRef>> ordered = snapshot.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null && e.getValue().id() != null && e.getValue().type() != null)
                .sorted(Comparator
                        .comparing((Map.Entry<MealPlan.SlotKey, MealPlan.SlotRef> e) -> e.getKey().dayOfWeek().getValue())
                        .thenComparing(e -> mealTypeOrder(e.getKey().mealType()))
                )
                .toList();

        for (Map.Entry<MealPlan.SlotKey, MealPlan.SlotRef> e : ordered) {
            MealPlan.SlotKey key = e.getKey();
            MealPlan.SlotRef ref = e.getValue();
            SlotRefKey refKey = SlotRefKey.of(ref);

            ResolvedRecipe rr = (refKey == null) ? null : resolved.byRef().get(refKey);
            if (rr == null) continue;

            String k = key.mealType().name() + "-" + key.dayOfWeek().name();
            slots.put(k, new GetMealPlanResDTO.SlotSummaryDTO(
                    toDtoSlotRefType(ref.type()),
                    ref.id(),
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

        Set<SlotRefKey> refKeys = new HashSet<>();
        for (MealPlan mp : plans) {
            Map<MealPlan.SlotKey, MealPlan.SlotRef> snap = mp.snapshotAllSlotRefs();
            if (snap == null) continue;
            for (MealPlan.SlotRef ref : snap.values()) {
                SlotRefKey k = SlotRefKey.of(ref);
                if (k != null) refKeys.add(k);
            }
        }

        ResolvedMaps resolved = resolveStoredRefs(familyRoomId, refKeys);

        List<GetMealPlanResDTO.WeekHistoryDTO> weeks = new ArrayList<>();
        for (MealPlan mp : plans) {
            Map<MealPlan.SlotKey, MealPlan.SlotRef> snapshot = mp.snapshotAllSlotRefs();
            if (snapshot == null) continue;

            Map<DayOfWeek, List<GetMealPlanResDTO.HistoryMealDTO>> dayMap = new EnumMap<>(DayOfWeek.class);

            for (Map.Entry<MealPlan.SlotKey, MealPlan.SlotRef> e : snapshot.entrySet()) {
                MealPlan.SlotKey key = e.getKey();
                MealPlan.SlotRef ref = e.getValue();
                if (key == null || ref == null || ref.id() == null || ref.type() == null) continue;
                SlotRefKey refKey = SlotRefKey.of(ref);

                ResolvedRecipe rr = (refKey == null) ? null : resolved.byRef().get(refKey);
                if (rr == null) continue;

                dayMap.computeIfAbsent(key.dayOfWeek(), d -> new ArrayList<>())
                        .add(new GetMealPlanResDTO.HistoryMealDTO(
                                key.mealType(),
                                toDtoSlotRefType(ref.type()),
                                ref.id(),
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

    private record SlotRefKey(MealPlan.SlotRefType type, Long id) {
        static SlotRefKey of(MealPlan.SlotRef ref) {
            if (ref == null || ref.type() == null || ref.id() == null) return null;
            return new SlotRefKey(ref.type(), ref.id());
        }
    }

    private record ResolvedRecipe(
            Long recipeId,
            Long transformedRecipeId,
            String title,
            String ingredients,
            String instructions
    ) {}

    private record ResolvedMaps(
            Map<SlotRefKey, ResolvedRecipe> byRef
    ) {}

    /** storedId(type+id) -> recipeId/transformedRecipeId/title/ingredients/instructions */
    private ResolvedMaps resolveStoredRefs(Long familyRoomId, Set<SlotRefKey> refKeys) {
        if (refKeys == null || refKeys.isEmpty()) return new ResolvedMaps(Map.of());

        Set<Long> recipeIds = new HashSet<>();
        Set<Long> transformedIds = new HashSet<>();

        for (SlotRefKey k : refKeys) {
            if (k == null || k.id() == null || k.type() == null) continue;
            if (k.type() == MealPlan.SlotRefType.RECIPE) recipeIds.add(k.id());
            else if (k.type() == MealPlan.SlotRefType.TRANSFORMED_RECIPE) transformedIds.add(k.id());
        }

        if (recipeIds.isEmpty() && transformedIds.isEmpty()) return new ResolvedMaps(Map.of());

        Map<SlotRefKey, ResolvedRecipe> resolved = new HashMap<>();

        // 1) TRANSFORMED_RECIPE: 반드시 familyRoomId로 스코프
        Map<Long, TransformedRecipe> trById = new HashMap<>();
        if (!transformedIds.isEmpty()) {
            List<TransformedRecipe> trs = transformedRecipeRepository.findAllByFamilyRoomIdAndIdIn(familyRoomId, transformedIds);
            for (TransformedRecipe tr : trs) {
                if (tr == null || tr.getId() == null) continue;
                trById.put(tr.getId(), tr);
            }
        }

        // base recipe ids from transformed
        Set<Long> baseRecipeIds = new HashSet<>();
        for (TransformedRecipe tr : trById.values()) {
            Recipe base = tr.getBaseRecipe();
            if (base != null && base.getId() != null) baseRecipeIds.add(base.getId());
        }

        // load recipes: direct recipe slots + transformed base recipes
        Set<Long> allRecipeIdsToLoad = new HashSet<>(recipeIds);
        allRecipeIdsToLoad.addAll(baseRecipeIds);

        Map<Long, Recipe> recipeById = allRecipeIdsToLoad.isEmpty()
                ? Map.of()
                : recipeRepository.findAllById(allRecipeIdsToLoad)
                .stream()
                .filter(Objects::nonNull)
                .filter(r -> r.getId() != null)
                .collect(Collectors.toMap(Recipe::getId, r -> r, (a, b) -> a));

        // recipeId로 저장된 케이스는 transformedRecipeId도 내려주기 위해 조회 (옵션 유지)
        Map<Long, Long> transformedIdByRecipeId = new HashMap<>();
        if (!recipeIds.isEmpty()) {
            transformedRecipeRepository.findByFamilyRoomIdAndBaseRecipe_IdIn(familyRoomId, recipeIds)
                    .stream()
                    .filter(tr -> tr != null && tr.getId() != null)
                    .filter(tr -> tr.getBaseRecipe() != null && tr.getBaseRecipe().getId() != null)
                    .forEach(tr -> transformedIdByRecipeId.put(tr.getBaseRecipe().getId(), tr.getId()));
        }

        // resolved: storedId == transformedRecipeId
        for (Long storedId : transformedIds) {
            TransformedRecipe tr = trById.get(storedId);
            if (tr == null) continue;
            Recipe base = (tr.getBaseRecipe() == null) ? null : recipeById.get(tr.getBaseRecipe().getId());
            if (base == null) continue;

            String title = (tr.getTitle() == null || tr.getTitle().isBlank()) ? base.getTitle() : tr.getTitle();

            resolved.put(new SlotRefKey(MealPlan.SlotRefType.TRANSFORMED_RECIPE, storedId), new ResolvedRecipe(
                    base.getId(),
                    tr.getId(),
                    title,
                    pickIngredients(tr, base),
                    pickInstructions(tr, base)
            ));
        }

        // resolved: storedId == recipeId
        for (Long storedId : recipeIds) {
            Recipe r = recipeById.get(storedId);
            if (r == null) continue;

            resolved.put(new SlotRefKey(MealPlan.SlotRefType.RECIPE, storedId), new ResolvedRecipe(
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
        String v = tr == null ? null : tr.getIngredientsRaw();
        return (v == null || v.isBlank()) ? (base == null ? null : base.getIngredientsRaw()) : v;
    }

    private String pickInstructions(TransformedRecipe tr, Recipe base) {
        String v = tr == null ? null : tr.getInstructionsRaw();
        return (v == null || v.isBlank()) ? (base == null ? null : base.getInstructionsRaw()) : v;
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

    private static GetMealPlanResDTO.SlotRefType toDtoSlotRefType(MealPlan.SlotRefType type) {
        if (type == null) throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_RECIPE_NOT_FOUND);
        return (type == MealPlan.SlotRefType.TRANSFORMED_RECIPE)
                ? GetMealPlanResDTO.SlotRefType.TRANSFORMED_RECIPE
                : GetMealPlanResDTO.SlotRefType.RECIPE;
    }
}
