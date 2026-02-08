package com.urisik.backend.domain.mealplan.service;

import com.urisik.backend.domain.recipe.entity.RecipeStep;
import com.urisik.backend.domain.recipe.repository.RecipeStepRepository;

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
import java.time.ZoneId;
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
    private final RecipeStepRepository recipeStepRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 오늘의 식단 조회 */
    @Transactional(readOnly = true)
    public GetMealPlanResDTO.TodayMealPlanResDTO getTodayMealPlan(Long memberId, Long familyRoomId) {
        familyRoomService.validateMember(memberId, familyRoomId);

        LocalDate today = LocalDate.now(KST);
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
        Map<MealPlan.SlotKey, MealPlan.SlotRef> snapshot = mealPlan.snapshotAllSlotRefs();
        if (snapshot != null && !snapshot.isEmpty()) {
            snapshot.entrySet().stream()
                    .filter(e -> e.getKey() != null && e.getValue() != null)
                    .filter(e -> e.getKey().dayOfWeek() == dow)
                    .filter(e -> mealTypes.contains(e.getKey().mealType()))
                    .sorted(Comparator.comparing(e -> mealTypeOrder(e.getKey().mealType())))
                    .forEach(e -> {
                        MealPlan.SlotRef ref = e.getValue();
                        if (ref != null && ref.id() != null && ref.type() != null) {
                            storedRefsByMealType.put(e.getKey().mealType(), ref);
                        }
                    });
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

                    String title = (rr == null) ? "(레시피 정보를 불러올 수 없음)" : rr.title();
                    String ingredients = (rr == null) ? "" : rr.ingredients();

                    return new GetMealPlanResDTO.TodayMealDTO(
                            e.getKey(),
                            toDtoSlotRefType(ref.type()),
                            ref.id(),
                            title,
                            rr == null ? null : rr.imageUrl(),
                            ingredients,
                            rr == null || rr.recipeSteps() == null ? List.of() : rr.recipeSteps()
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

        LocalDate weekStart = normalizeToMonday(anyDateInWeek == null ? LocalDate.now(KST) : anyDateInWeek);

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

            String title = (rr == null) ? "(레시피 정보를 불러올 수 없음)" : rr.title();
            String ingredients = (rr == null) ? "" : rr.ingredients();

            String k = key.dayOfWeek().name() + "-" + key.mealType().name();
            slots.put(k, new GetMealPlanResDTO.SlotSummaryDTO(
                    toDtoSlotRefType(ref.type()),
                    ref.id(),
                    title,
                    rr == null ? null : rr.imageUrl(),
                    ingredients
            ));
        }

        return new GetMealPlanResDTO.WeeklyMealPlanResDTO(mealPlan.getId(), mealPlan.getWeekStartDate(), slots);
    }

    /** 최근 1개월 식단 조회 */
    @Transactional(readOnly = true)
    public GetMealPlanResDTO.MonthlyMealPlanResDTO getLastMonthMealPlan(Long memberId, Long familyRoomId) {
        familyRoomService.validateMember(memberId, familyRoomId);

        LocalDate to = LocalDate.now(KST);
        LocalDate from = to.minusMonths(1);

        LocalDate startWeek = normalizeToMonday(from);
        LocalDate endWeek = normalizeToMonday(to);

        List<MealPlan> plans = mealPlanRepository
                .findAllByFamilyRoomIdAndWeekStartDateBetweenOrderByWeekStartDateDesc(
                        familyRoomId, startWeek, endWeek
                )
                .stream()
                .filter(mp -> mp.getStatus() == MealPlanStatus.CONFIRMED)
                .sorted(Comparator.comparing(MealPlan::getWeekStartDate))
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

                String title = (rr == null) ? "(레시피 정보를 불러올 수 없음)" : rr.title();
                String ingredients = (rr == null) ? "" : rr.ingredients();

                dayMap.computeIfAbsent(key.dayOfWeek(), d -> new ArrayList<>())
                        .add(new GetMealPlanResDTO.HistoryMealDTO(
                                key.mealType(),
                                toDtoSlotRefType(ref.type()),
                                ref.id(),
                                title,
                                rr == null ? null : rr.imageUrl(),
                                ingredients
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
            String imageUrl,
            String ingredients,
            String instructions,
            List<GetMealPlanResDTO.RecipeStepDTO> recipeSteps
    ) {
    }

    private record ResolvedMaps(
            Map<SlotRefKey, ResolvedRecipe> byRef
    ) {
    }

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

        // TRANSFORMED_RECIPE
        Map<Long, TransformedRecipe> trById = new HashMap<>();
        if (!transformedIds.isEmpty()) {
            List<TransformedRecipe> trs = transformedRecipeRepository.findAllById(transformedIds);
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

        // recipeId로 저장된 케이스는 transformedRecipeId도 내려주기 위해 조회
        Map<Long, Long> transformedIdByRecipeId = new HashMap<>();
        if (!recipeIds.isEmpty()) {
            transformedRecipeRepository.findByFamilyRoomIdAndBaseRecipe_IdIn(familyRoomId, recipeIds)
                    .stream()
                    .filter(tr -> tr != null && tr.getId() != null)
                    .filter(tr -> tr.getBaseRecipe() != null && tr.getBaseRecipe().getId() != null)
                    .forEach(tr -> transformedIdByRecipeId.put(tr.getBaseRecipe().getId(), tr.getId()));
        }

        // Load recipe steps for all relevant recipeIds (canonical and base)
        Map<Long, List<GetMealPlanResDTO.RecipeStepDTO>> stepsByRecipeId = new HashMap<>();
        if (!allRecipeIdsToLoad.isEmpty()) {
            List<RecipeStep> steps = recipeStepRepository
                    .findAllByRecipe_IdInOrderByRecipe_IdAscStepOrderAsc(allRecipeIdsToLoad);

            for (RecipeStep s : steps) {
                if (s == null || s.getRecipe() == null || s.getRecipe().getId() == null) continue;
                Long rid = s.getRecipe().getId();
                stepsByRecipeId
                        .computeIfAbsent(rid, k -> new ArrayList<>())
                        .add(new GetMealPlanResDTO.RecipeStepDTO(
                                s.getStepOrder(),
                                s.getDescription(),
                                s.getImageUrl()
                        ));
            }
        }

        // resolved: storedId == transformedRecipeId
        for (Long storedId : transformedIds) {
            TransformedRecipe tr = trById.get(storedId);
            if (tr == null) continue;

            Recipe base = (tr.getBaseRecipe() == null) ? null : recipeById.get(tr.getBaseRecipe().getId());

            String title;
            if (tr.getTitle() != null && !tr.getTitle().isBlank()) {
                title = tr.getTitle();
            } else if (base != null) {
                title = base.getTitle();
            } else {
                title = "(레시피 정보를 불러올 수 없음)";
            }

            String imageUrl = null;
            if (base != null && base.getRecipeExternalMetadata() != null) {
                imageUrl = base.getRecipeExternalMetadata().getThumbnailImageUrl();
            }

            Long baseRecipeId = (tr.getBaseRecipe() == null ? null : tr.getBaseRecipe().getId());
            List<GetMealPlanResDTO.RecipeStepDTO> baseSteps = (baseRecipeId == null)
                    ? List.of()
                    : stepsByRecipeId.getOrDefault(baseRecipeId, List.of());

            // Fallback: if metadata image is missing, use the first step image from the base recipe
            if ((imageUrl == null || imageUrl.isBlank()) && !baseSteps.isEmpty()) {
                String stepImg = baseSteps.get(0).imageUrl();
                if (stepImg != null && !stepImg.isBlank()) {
                    imageUrl = stepImg;
                }
            }

            List<String> trStepDescriptions = splitInstructionsToSteps(tr.getInstructionsRaw());
            List<GetMealPlanResDTO.RecipeStepDTO> steps = buildTransformedSteps(baseSteps, trStepDescriptions);
            steps = fillStepImageWithTitleImage(steps, imageUrl);

            resolved.put(new SlotRefKey(MealPlan.SlotRefType.TRANSFORMED_RECIPE, storedId), new ResolvedRecipe(
                    baseRecipeId,
                    tr.getId(),
                    title,
                    imageUrl,
                    base == null ? (tr.getIngredientsRaw() == null ? "" : tr.getIngredientsRaw()) : pickIngredients(tr, base),
                    tr.getInstructionsRaw() == null ? "" : tr.getInstructionsRaw(),
                    steps
            ));
        }

        // resolved: storedId == recipeId
        for (Long storedId : recipeIds) {
            Recipe r = recipeById.get(storedId);
            if (r == null) continue;

            String imageUrl = null;
            if (r.getRecipeExternalMetadata() != null) {
                imageUrl = r.getRecipeExternalMetadata().getThumbnailImageUrl();
            }

            List<GetMealPlanResDTO.RecipeStepDTO> steps =
                    (r.getId() == null) ? List.of() : stepsByRecipeId.getOrDefault(r.getId(), List.of());

            // If recipe_step rows are missing, fall back to splitting instructions_raw into step descriptions.
            if (steps == null || steps.isEmpty()) {
                List<String> descs = splitInstructionsToSteps(r.getInstructionsRaw());
                steps = buildStepsFromDescriptions(descs);
            }

            // Ensure step imageUrl is always present when we have a title image.
            steps = fillStepImageWithTitleImage(steps, imageUrl);

            boolean hasStepDescription = steps.stream()
                    .anyMatch(s -> s != null && s.description() != null && !s.description().isBlank());

            String instructions = hasStepDescription
                    ? ""
                    : (r.getInstructionsRaw() == null ? "" : r.getInstructionsRaw());

            resolved.put(new SlotRefKey(MealPlan.SlotRefType.RECIPE, storedId), new ResolvedRecipe(
                    r.getId(),
                    transformedIdByRecipeId.get(r.getId()),
                    r.getTitle(),
                    imageUrl,
                    r.getIngredientsRaw(),
                    instructions,
                    steps
            ));
        }

        return new ResolvedMaps(resolved);
    }

    private static List<GetMealPlanResDTO.RecipeStepDTO> fillStepImageWithTitleImage(
            List<GetMealPlanResDTO.RecipeStepDTO> steps,
            String titleImageUrl
    ) {
        if (steps == null || steps.isEmpty()) return List.of();
        if (titleImageUrl == null || titleImageUrl.isBlank()) return steps;

        return steps.stream()
                .map(s -> {
                    if (s == null) return null;
                    String img = s.imageUrl();
                    if (img != null && !img.isBlank()) return s;
                    return new GetMealPlanResDTO.RecipeStepDTO(
                            s.stepOrder(),
                            s.description(),
                            titleImageUrl
                    );
                })
                .toList();
    }

    private static List<GetMealPlanResDTO.RecipeStepDTO> buildStepsFromDescriptions(List<String> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) return List.of();

        List<GetMealPlanResDTO.RecipeStepDTO> out = new ArrayList<>(descriptions.size());
        for (int i = 0; i < descriptions.size(); i++) {
            String d = descriptions.get(i);
            if (d == null || d.isBlank()) continue;
            out.add(new GetMealPlanResDTO.RecipeStepDTO(i + 1, d, null));
        }
        return out;
    }

    private String pickIngredients(TransformedRecipe tr, Recipe base) {
        String v = tr == null ? null : tr.getIngredientsRaw();
        return (v == null || v.isBlank()) ? (base == null ? null : base.getIngredientsRaw()) : v;
    }

    private static List<String> splitInstructionsToSteps(String raw) {
        if (raw == null) return List.of();
        String text = raw.trim();
        if (text.isEmpty()) return List.of();

        // Numbered patterns at line start: 1. / 1) / 1 - / 1: / 1]
        List<Integer> starts = new ArrayList<>();
        var p = java.util.regex.Pattern.compile("(?m)^\\s*\\d{1,2}\\s*(?:[\\.)\\]:-])\\s*");
        var m = p.matcher(text);
        while (m.find()) starts.add(m.start());

        if (starts.size() >= 2) {
            List<String> chunks = new ArrayList<>();
            for (int i = 0; i < starts.size(); i++) {
                int s = starts.get(i);
                int e = (i + 1 < starts.size()) ? starts.get(i + 1) : text.length();
                String chunk = text.substring(s, e).trim();
                if (!chunk.isEmpty()) chunks.add(chunk);
            }
            return chunks;
        }

        // Bulleted patterns: - / • / *
        List<String> bullets = Arrays.stream(text.split("(?m)^\\s*(?:[-•*])\\s+"))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .toList();
        if (bullets.size() >= 2) return bullets;

        // Fallback: split by non-empty lines
        List<String> lines = Arrays.stream(text.split("\\r?\\n"))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .toList();
        if (lines.size() >= 2) return lines;

        return List.of(text);
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
        if (date == null) return normalizeToMonday(LocalDate.now(KST));
        int diff = date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        return date.minusDays(diff);
    }

    private static GetMealPlanResDTO.SlotRefType toDtoSlotRefType(MealPlan.SlotRefType type) {
        if (type == null) throw new MealPlanException(MealPlanErrorCode.MEAL_PLAN_RECIPE_NOT_FOUND);
        return (type == MealPlan.SlotRefType.TRANSFORMED_RECIPE)
                ? GetMealPlanResDTO.SlotRefType.TRANSFORMED_RECIPE
                : GetMealPlanResDTO.SlotRefType.RECIPE;
    }

    // --- Inserted: buildTransformedSteps ---
    private static List<GetMealPlanResDTO.RecipeStepDTO> buildTransformedSteps(
            List<GetMealPlanResDTO.RecipeStepDTO> baseSteps,
            List<String> transformedDescriptions
    ) {
        List<GetMealPlanResDTO.RecipeStepDTO> safeBase = (baseSteps == null) ? List.of() : baseSteps;
        List<String> descs = (transformedDescriptions == null) ? List.of() : transformedDescriptions;

        // If we have base steps (with imageUrl), align transformed descriptions by index.
        if (!safeBase.isEmpty()) {
            int baseSize = safeBase.size();
            int descSize = descs.size();
            int total = Math.max(baseSize, descSize);

            // Determine the last stepOrder from base (fallback to baseSize)
            int lastOrder = baseSize;
            for (int i = baseSize - 1; i >= 0; i--) {
                GetMealPlanResDTO.RecipeStepDTO b = safeBase.get(i);
                if (b != null && b.stepOrder() > 0) {
                    lastOrder = b.stepOrder();
                    break;
                }
            }

            List<GetMealPlanResDTO.RecipeStepDTO> out = new ArrayList<>(total);

            for (int i = 0; i < total; i++) {
                if (i < baseSize) {
                    GetMealPlanResDTO.RecipeStepDTO b = safeBase.get(i);
                    int order = (b == null) ? (i + 1) : b.stepOrder();
                    String imageUrl = (b == null) ? null : b.imageUrl();
                    String description = (i < descSize) ? descs.get(i) : null;
                    out.add(new GetMealPlanResDTO.RecipeStepDTO(order, description, imageUrl));
                } else {
                    // description is longer than baseSteps -> append extra steps
                    int order = lastOrder + (i - baseSize + 1);
                    String description = descs.get(i);
                    out.add(new GetMealPlanResDTO.RecipeStepDTO(order, description, null));
                }
            }
            return out;
        }

        // If no base steps exist yet, fall back to transformed text only.
        if (!descs.isEmpty()) {
            List<GetMealPlanResDTO.RecipeStepDTO> out = new ArrayList<>(descs.size());
            for (int i = 0; i < descs.size(); i++) {
                out.add(new GetMealPlanResDTO.RecipeStepDTO(i + 1, descs.get(i), null));
            }
            return out;
        }

        return List.of();
    }
}
