package com.urisik.backend.domain.mealplan.entity;

import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.mealplan.enums.MealPlanStatus;
import com.urisik.backend.domain.mealplan.enums.MealType;
import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

/**
 * 주간 식단(가족 기준) 엔티티
 */
@Entity
@Table(
        name = "meal_plan",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_meal_plan_family_room_week_start",
                        columnNames = {"family_room_id", "week_start_date"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MealPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MealPlanStatus status;

    /** 슬롯에 저장된 레퍼런스 타입(Recipe vs TransformedRecipe) */
    public enum SlotRefType {
        RECIPE,
        TRANSFORMED_RECIPE
    }

    // 점심
    @Column(name = "monday_lunch")    private Long mondayLunch;
    @Column(name = "tuesday_lunch")   private Long tuesdayLunch;
    @Column(name = "wednesday_lunch") private Long wednesdayLunch;
    @Column(name = "thursday_lunch")  private Long thursdayLunch;
    @Column(name = "friday_lunch")    private Long fridayLunch;
    @Column(name = "saturday_lunch")  private Long saturdayLunch;
    @Column(name = "sunday_lunch")    private Long sundayLunch;

    // 점심 - 타입(Recipe vs TransformedRecipe)
    @Enumerated(EnumType.STRING)
    @Column(name = "monday_lunch_type")    private SlotRefType mondayLunchType;
    @Enumerated(EnumType.STRING)
    @Column(name = "tuesday_lunch_type")   private SlotRefType tuesdayLunchType;
    @Enumerated(EnumType.STRING)
    @Column(name = "wednesday_lunch_type") private SlotRefType wednesdayLunchType;
    @Enumerated(EnumType.STRING)
    @Column(name = "thursday_lunch_type")  private SlotRefType thursdayLunchType;
    @Enumerated(EnumType.STRING)
    @Column(name = "friday_lunch_type")    private SlotRefType fridayLunchType;
    @Enumerated(EnumType.STRING)
    @Column(name = "saturday_lunch_type")  private SlotRefType saturdayLunchType;
    @Enumerated(EnumType.STRING)
    @Column(name = "sunday_lunch_type")    private SlotRefType sundayLunchType;

    // 저녁
    @Column(name = "monday_dinner")    private Long mondayDinner;
    @Column(name = "tuesday_dinner")   private Long tuesdayDinner;
    @Column(name = "wednesday_dinner") private Long wednesdayDinner;
    @Column(name = "thursday_dinner")  private Long thursdayDinner;
    @Column(name = "friday_dinner")    private Long fridayDinner;
    @Column(name = "saturday_dinner")  private Long saturdayDinner;
    @Column(name = "sunday_dinner")    private Long sundayDinner;

    // 저녁 - 타입(Recipe vs TransformedRecipe)
    @Enumerated(EnumType.STRING)
    @Column(name = "monday_dinner_type")    private SlotRefType mondayDinnerType;
    @Enumerated(EnumType.STRING)
    @Column(name = "tuesday_dinner_type")   private SlotRefType tuesdayDinnerType;
    @Enumerated(EnumType.STRING)
    @Column(name = "wednesday_dinner_type") private SlotRefType wednesdayDinnerType;
    @Enumerated(EnumType.STRING)
    @Column(name = "thursday_dinner_type")  private SlotRefType thursdayDinnerType;
    @Enumerated(EnumType.STRING)
    @Column(name = "friday_dinner_type")    private SlotRefType fridayDinnerType;
    @Enumerated(EnumType.STRING)
    @Column(name = "saturday_dinner_type")  private SlotRefType saturdayDinnerType;
    @Enumerated(EnumType.STRING)
    @Column(name = "sunday_dinner_type")    private SlotRefType sundayDinnerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_room_id", nullable = false)
    private FamilyRoom familyRoom;

    /**
     * 사용자가 선택한 슬롯 목록
     * - 값(레시피 id)이 아직 채워지기 전이라도 선택 자체는 보존되어야 하므로 별도 저장
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "meal_plan_selected_slot",
            joinColumns = @JoinColumn(name = "meal_plan_id")
    )
    @Builder.Default
    private Set<SelectedSlot> selectedSlots = new HashSet<>();

    @Embeddable
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class SelectedSlot {
        @Enumerated(EnumType.STRING)
        @Column(name = "meal_type", nullable = false)
        private MealType mealType;

        @Enumerated(EnumType.STRING)
        @Column(name = "day_of_week", nullable = false)
        private DayOfWeek dayOfWeek;

        public SlotKey toSlotKey() {
            return new SlotKey(mealType, dayOfWeek);
        }

        public static SelectedSlot from(SlotKey key) {
            Objects.requireNonNull(key, "slotKey");
            return new SelectedSlot(key.mealType(), key.dayOfWeek());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SelectedSlot that = (SelectedSlot) o;
            return mealType == that.mealType && dayOfWeek == that.dayOfWeek;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mealType, dayOfWeek);
        }
    }

    /** MealType, DayOfWeek 조합을 안전하게 다루기 위한 키 */
    public record SlotKey(MealType mealType, DayOfWeek dayOfWeek) {
        public SlotKey {
            Objects.requireNonNull(mealType, "mealType");
            Objects.requireNonNull(dayOfWeek, "dayOfWeek");
        }
    }

    /** 슬롯에 저장된 id(Recipe/TransformedRecipe) 조회 */
    public Long getSlot(MealType mealType, DayOfWeek dayOfWeek) {
        return switch (mealType) {
            case LUNCH -> switch (dayOfWeek) {
                case MONDAY -> mondayLunch;
                case TUESDAY -> tuesdayLunch;
                case WEDNESDAY -> wednesdayLunch;
                case THURSDAY -> thursdayLunch;
                case FRIDAY -> fridayLunch;
                case SATURDAY -> saturdayLunch;
                case SUNDAY -> sundayLunch;
            };
            case DINNER -> switch (dayOfWeek) {
                case MONDAY -> mondayDinner;
                case TUESDAY -> tuesdayDinner;
                case WEDNESDAY -> wednesdayDinner;
                case THURSDAY -> thursdayDinner;
                case FRIDAY -> fridayDinner;
                case SATURDAY -> saturdayDinner;
                case SUNDAY -> sundayDinner;
            };
        };
    }

    /** 슬롯에 저장된 타입(Recipe/TransformedRecipe) 조회 */
    public SlotRefType getSlotType(MealType mealType, DayOfWeek dayOfWeek) {
        return switch (mealType) {
            case LUNCH -> switch (dayOfWeek) {
                case MONDAY -> mondayLunchType;
                case TUESDAY -> tuesdayLunchType;
                case WEDNESDAY -> wednesdayLunchType;
                case THURSDAY -> thursdayLunchType;
                case FRIDAY -> fridayLunchType;
                case SATURDAY -> saturdayLunchType;
                case SUNDAY -> sundayLunchType;
            };
            case DINNER -> switch (dayOfWeek) {
                case MONDAY -> mondayDinnerType;
                case TUESDAY -> tuesdayDinnerType;
                case WEDNESDAY -> wednesdayDinnerType;
                case THURSDAY -> thursdayDinnerType;
                case FRIDAY -> fridayDinnerType;
                case SATURDAY -> saturdayDinnerType;
                case SUNDAY -> sundayDinnerType;
            };
        };
    }

    /** SlotKey 기반 타입 조회 */
    public SlotRefType getSlotType(SlotKey slotKey) {
        Objects.requireNonNull(slotKey, "slotKey");
        return getSlotType(slotKey.mealType(), slotKey.dayOfWeek());
    }

    /** 슬롯에 저장된 (type,id) 조회 */
    public record SlotRef(SlotRefType type, Long id) {
        public SlotRef {
            // type/id 모두 null인 경우(미선택/미할당)도 허용
        }
    }

    /** 슬롯에 저장된 (type,id) 조회 */
    public SlotRef getSlotRef(MealType mealType, DayOfWeek dayOfWeek) {
        return new SlotRef(getSlotType(mealType, dayOfWeek), getSlot(mealType, dayOfWeek));
    }

    /** SlotKey 기반 (type,id) 조회 */
    public SlotRef getSlotRef(SlotKey slotKey) {
        Objects.requireNonNull(slotKey, "slotKey");
        return getSlotRef(slotKey.mealType(), slotKey.dayOfWeek());
    }

    /** SlotKey 기반 조회 */
    public Long getSlot(SlotKey slotKey) {
        Objects.requireNonNull(slotKey, "slotKey");
        return getSlot(slotKey.mealType(), slotKey.dayOfWeek());
    }

    /** 선택 슬롯 여부(= UI에서 선택한 칸인지) */
    public boolean isSelectedSlot(SlotKey slotKey) {
        Objects.requireNonNull(slotKey, "slotKey");
        if (selectedSlots == null || selectedSlots.isEmpty()) return false;
        return selectedSlots.contains(SelectedSlot.from(slotKey));
    }

    /**
     * SlotKey 기반 단일 슬롯 업데이트
     * - DRAFT/권한/안전성 등은 Service에서 검증
     */
    public void updateSlot(SlotKey slotKey, SlotRefType refType, Long recipeRefId) {
        Objects.requireNonNull(slotKey, "slotKey");
        Objects.requireNonNull(refType, "refType");
        Objects.requireNonNull(recipeRefId, "recipeRefId");

        // 수정 시 선택 슬롯을 보존
        selectedSlots.add(SelectedSlot.from(slotKey));
        setSlot(slotKey.mealType(), slotKey.dayOfWeek(), refType, recipeRefId);
    }

    /**
     * 하위호환(기존 코드 컴파일 유지용)
     * - 타입을 저장할 수 없으므로 RECIPE로 저장한다.
     */
    @Deprecated
    public void updateSlot(SlotKey slotKey, Long recipeRefId) {
        updateSlot(slotKey, SlotRefType.RECIPE, recipeRefId);
    }

    /** 슬롯에 (type,id) 설정 */
    public void setSlot(MealType mealType, DayOfWeek dayOfWeek, SlotRefType refType, Long recipeRefId) {
        // id/type 모두 null 가능(리셋 시)
        switch (mealType) {
            case LUNCH -> {
                switch (dayOfWeek) {
                    case MONDAY -> { mondayLunch = recipeRefId; mondayLunchType = refType; }
                    case TUESDAY -> { tuesdayLunch = recipeRefId; tuesdayLunchType = refType; }
                    case WEDNESDAY -> { wednesdayLunch = recipeRefId; wednesdayLunchType = refType; }
                    case THURSDAY -> { thursdayLunch = recipeRefId; thursdayLunchType = refType; }
                    case FRIDAY -> { fridayLunch = recipeRefId; fridayLunchType = refType; }
                    case SATURDAY -> { saturdayLunch = recipeRefId; saturdayLunchType = refType; }
                    case SUNDAY -> { sundayLunch = recipeRefId; sundayLunchType = refType; }
                }
            }
            case DINNER -> {
                switch (dayOfWeek) {
                    case MONDAY -> { mondayDinner = recipeRefId; mondayDinnerType = refType; }
                    case TUESDAY -> { tuesdayDinner = recipeRefId; tuesdayDinnerType = refType; }
                    case WEDNESDAY -> { wednesdayDinner = recipeRefId; wednesdayDinnerType = refType; }
                    case THURSDAY -> { thursdayDinner = recipeRefId; thursdayDinnerType = refType; }
                    case FRIDAY -> { fridayDinner = recipeRefId; fridayDinnerType = refType; }
                    case SATURDAY -> { saturdayDinner = recipeRefId; saturdayDinnerType = refType; }
                    case SUNDAY -> { sundayDinner = recipeRefId; sundayDinnerType = refType; }
                }
            }
        }
    }

    /** 하위호환(기존 코드 컴파일 유지용): 타입을 RECIPE로 저장 */
    @Deprecated
    public void setSlot(MealType mealType, DayOfWeek dayOfWeek, Long recipeRefId) {
        setSlot(mealType, dayOfWeek, recipeRefId == null ? null : SlotRefType.RECIPE, recipeRefId);
    }

    /** SlotKey 기반 (type,id) 설정 */
    public void setSlot(SlotKey slotKey, SlotRefType refType, Long recipeRefId) {
        Objects.requireNonNull(slotKey, "slotKey");
        setSlot(slotKey.mealType(), slotKey.dayOfWeek(), refType, recipeRefId);
    }

    /** 선택된 슬롯만 채우고, 선택되지 않은 슬롯은 null */
    public void applySelectedSlots(Map<SlotKey, Long> selectedAssignments) {
        // lunch reset
        mondayLunch = null;
        tuesdayLunch = null;
        wednesdayLunch = null;
        thursdayLunch = null;
        fridayLunch = null;
        saturdayLunch = null;
        sundayLunch = null;
        mondayLunchType = null;
        tuesdayLunchType = null;
        wednesdayLunchType = null;
        thursdayLunchType = null;
        fridayLunchType = null;
        saturdayLunchType = null;
        sundayLunchType = null;

        // dinner reset
        mondayDinner = null;
        tuesdayDinner = null;
        wednesdayDinner = null;
        thursdayDinner = null;
        fridayDinner = null;
        saturdayDinner = null;
        sundayDinner = null;
        mondayDinnerType = null;
        tuesdayDinnerType = null;
        wednesdayDinnerType = null;
        thursdayDinnerType = null;
        fridayDinnerType = null;
        saturdayDinnerType = null;
        sundayDinnerType = null;

        // selected reset
        if (selectedSlots == null) {
            selectedSlots = new HashSet<>();
        } else {
            selectedSlots.clear();
        }

        if (selectedAssignments == null || selectedAssignments.isEmpty()) {
            return;
        }

        for (Map.Entry<SlotKey, Long> e : selectedAssignments.entrySet()) {
            if (e.getKey() == null) {
                continue;
            }

            // 선택 슬롯은 값 유무와 상관없이 저장
            selectedSlots.add(SelectedSlot.from(e.getKey()));

            // 값이 있으면 실제 슬롯에 반영
            if (e.getValue() != null) {
                // 기존 API는 Long만 넘기므로 타입은 RECIPE로 저장
                setSlot(e.getKey().mealType(), e.getKey().dayOfWeek(), SlotRefType.RECIPE, e.getValue());
            }
        }
    }

    /** 엔티티의 전체 슬롯 스냅샷(Map) */
    public Map<SlotKey, Long> snapshotAllSlots() {
        Map<SlotKey, Long> map = new HashMap<>();

        // lunch
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.MONDAY), mondayLunch);
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.TUESDAY), tuesdayLunch);
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.WEDNESDAY), wednesdayLunch);
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.THURSDAY), thursdayLunch);
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.FRIDAY), fridayLunch);
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.SATURDAY), saturdayLunch);
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.SUNDAY), sundayLunch);

        // dinner
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.MONDAY), mondayDinner);
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.TUESDAY), tuesdayDinner);
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.WEDNESDAY), wednesdayDinner);
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.THURSDAY), thursdayDinner);
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.FRIDAY), fridayDinner);
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.SATURDAY), saturdayDinner);
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.SUNDAY), sundayDinner);

        return map;
    }

    /** 엔티티의 전체 슬롯 스냅샷(Map) - type+id */
    public Map<SlotKey, SlotRef> snapshotAllSlotRefs() {
        Map<SlotKey, SlotRef> map = new HashMap<>();

        // lunch
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.MONDAY), new SlotRef(mondayLunchType, mondayLunch));
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.TUESDAY), new SlotRef(tuesdayLunchType, tuesdayLunch));
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.WEDNESDAY), new SlotRef(wednesdayLunchType, wednesdayLunch));
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.THURSDAY), new SlotRef(thursdayLunchType, thursdayLunch));
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.FRIDAY), new SlotRef(fridayLunchType, fridayLunch));
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.SATURDAY), new SlotRef(saturdayLunchType, saturdayLunch));
        map.put(new SlotKey(MealType.LUNCH, DayOfWeek.SUNDAY), new SlotRef(sundayLunchType, sundayLunch));

        // dinner
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.MONDAY), new SlotRef(mondayDinnerType, mondayDinner));
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.TUESDAY), new SlotRef(tuesdayDinnerType, tuesdayDinner));
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.WEDNESDAY), new SlotRef(wednesdayDinnerType, wednesdayDinner));
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.THURSDAY), new SlotRef(thursdayDinnerType, thursdayDinner));
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.FRIDAY), new SlotRef(fridayDinnerType, fridayDinner));
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.SATURDAY), new SlotRef(saturdayDinnerType, saturdayDinner));
        map.put(new SlotKey(MealType.DINNER, DayOfWeek.SUNDAY), new SlotRef(sundayDinnerType, sundayDinner));

        return map;
    }

    /** 생성 시 기본 상태를 DRAFT로 시작시키는 팩토리 */
    public static MealPlan draft(FamilyRoom familyRoom, LocalDate weekStartDate) {
        return MealPlan.builder()
                .familyRoom(familyRoom)
                .weekStartDate(weekStartDate)
                .status(MealPlanStatus.DRAFT)
                .build();
    }

    public void updateStatus(MealPlanStatus status) {
        this.status = status;
    }

    /** 선택된 슬롯 목록 */
    public Collection<SlotKey> getSelectedSlots() {
        if (selectedSlots == null || selectedSlots.isEmpty()) return java.util.List.of();
        return selectedSlots.stream()
                .map(SelectedSlot::toSlotKey)
                .toList();
    }

    /** slotKey에 저장된 id 반환 (type은 *_type 컬럼에 별도 저장됨) */
    public Long getSlotValue(SlotKey key) {
        return getSlot(key);
    }

    /** 확정 전 검증용: 선택된 슬롯이 1개 이상인지 */
    public boolean hasAnySelectedSlot() {
        return selectedSlots != null && !selectedSlots.isEmpty();
    }
}
