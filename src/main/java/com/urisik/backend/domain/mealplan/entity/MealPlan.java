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

    // 점심
    @Column(name = "monday_lunch")    private Long mondayLunch;
    @Column(name = "tuesday_lunch")   private Long tuesdayLunch;
    @Column(name = "wednesday_lunch") private Long wednesdayLunch;
    @Column(name = "thursday_lunch")  private Long thursdayLunch;
    @Column(name = "friday_lunch")    private Long fridayLunch;
    @Column(name = "saturday_lunch")  private Long saturdayLunch;
    @Column(name = "sunday_lunch")    private Long sundayLunch;

    // 저녁
    @Column(name = "monday_dinner")    private Long mondayDinner;
    @Column(name = "tuesday_dinner")   private Long tuesdayDinner;
    @Column(name = "wednesday_dinner") private Long wednesdayDinner;
    @Column(name = "thursday_dinner")  private Long thursdayDinner;
    @Column(name = "friday_dinner")    private Long fridayDinner;
    @Column(name = "saturday_dinner")  private Long saturdayDinner;
    @Column(name = "sunday_dinner")    private Long sundayDinner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_room_id", nullable = false)
    private FamilyRoom familyRoom;

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

    /** SlotKey 기반 조회 */
    public Long getSlot(SlotKey slotKey) {
        Objects.requireNonNull(slotKey, "slotKey");
        return getSlot(slotKey.mealType(), slotKey.dayOfWeek());
    }

    /** 선택 슬롯 여부 */
    public boolean isSelectedSlot(SlotKey slotKey) {
        return getSlot(slotKey) != null;
    }

    /**
     * SlotKey 기반 단일 슬롯 업데이트
     * - DRAFT/권한/안전성 등은 Service에서 검증
     */
    public void updateSlot(SlotKey slotKey, Long recipeRefId) {
        Objects.requireNonNull(slotKey, "slotKey");
        Objects.requireNonNull(recipeRefId, "recipeRefId");
        setSlot(slotKey.mealType(), slotKey.dayOfWeek(), recipeRefId);
    }

    /** 슬롯에 id(Recipe/TransformedRecipe) 설정 */
    public void setSlot(MealType mealType, DayOfWeek dayOfWeek, Long recipeRefId) {
        switch (mealType) {
            case LUNCH -> {
                switch (dayOfWeek) {
                    case MONDAY -> mondayLunch = recipeRefId;
                    case TUESDAY -> tuesdayLunch = recipeRefId;
                    case WEDNESDAY -> wednesdayLunch = recipeRefId;
                    case THURSDAY -> thursdayLunch = recipeRefId;
                    case FRIDAY -> fridayLunch = recipeRefId;
                    case SATURDAY -> saturdayLunch = recipeRefId;
                    case SUNDAY -> sundayLunch = recipeRefId;
                }
            }
            case DINNER -> {
                switch (dayOfWeek) {
                    case MONDAY -> mondayDinner = recipeRefId;
                    case TUESDAY -> tuesdayDinner = recipeRefId;
                    case WEDNESDAY -> wednesdayDinner = recipeRefId;
                    case THURSDAY -> thursdayDinner = recipeRefId;
                    case FRIDAY -> fridayDinner = recipeRefId;
                    case SATURDAY -> saturdayDinner = recipeRefId;
                    case SUNDAY -> sundayDinner = recipeRefId;
                }
            }
        }
    }

    /** SlotKey 기반 설정 */
    public void setSlot(SlotKey slotKey, Long recipeRefId) {
        Objects.requireNonNull(slotKey, "slotKey");
        setSlot(slotKey.mealType(), slotKey.dayOfWeek(), recipeRefId);
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

        // dinner reset
        mondayDinner = null;
        tuesdayDinner = null;
        wednesdayDinner = null;
        thursdayDinner = null;
        fridayDinner = null;
        saturdayDinner = null;
        sundayDinner = null;

        if (selectedAssignments == null || selectedAssignments.isEmpty()) {
            return;
        }

        for (Map.Entry<SlotKey, Long> e : selectedAssignments.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            setSlot(e.getKey().mealType(), e.getKey().dayOfWeek(), e.getValue());
        }
    }

    /**
     * 엔티티의 전체 슬롯 스냅샷(Map)
     */
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

    /**
     * 선택된 슬롯 목록
     * - 이 엔티티에서는 "선택"을 별도로 저장하지 않고, 값이 채워진 슬롯을 선택된 것으로 본다.
     */
    public Collection<SlotKey> getSelectedSlots() {
        return snapshotAllSlots().entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(Map.Entry::getKey)
                .toList();
    }

    /** slotKey에 저장된 id 반환 (Recipe 또는 TransformedRecipe) */
    public Long getSlotValue(SlotKey key) {
        return getSlot(key);
    }

    /** 확정 전 검증용: 선택된 슬롯이 1개 이상인지 */
    public boolean hasAnySelectedSlot() {
        return snapshotAllSlots().values().stream().anyMatch(Objects::nonNull);
    }
}
