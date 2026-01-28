package com.urisik.backend.domain.mealplan.entity;

import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.mealplan.enums.MealPlanStatus;
import com.urisik.backend.domain.mealplan.enums.MealType;
import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 주간 식단(가족 기준) 엔티티
 * 슬롯 컬럼(monday_lunch ~ sunday_dinner)은 가족 기준 변형 레시피(transformed_recipe)의 PK를 저장
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

    /**
     * MealType, DayOfWeek 조합을 안전하게 다루기 위한 키
     */
    public record SlotKey(MealType mealType, DayOfWeek dayOfWeek) {
        public SlotKey {
            Objects.requireNonNull(mealType, "mealType");
            Objects.requireNonNull(dayOfWeek, "dayOfWeek");
        }
    }

    /**
     * 슬롯에 저장된 transformedRecipeId 조회
     */
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

    /**
     * 슬롯에 transformedRecipeId 설정
     */
    public void setSlot(MealType mealType, DayOfWeek dayOfWeek, Long transformedRecipeId) {
        switch (mealType) {
            case LUNCH -> {
                switch (dayOfWeek) {
                    case MONDAY -> mondayLunch = transformedRecipeId;
                    case TUESDAY -> tuesdayLunch = transformedRecipeId;
                    case WEDNESDAY -> wednesdayLunch = transformedRecipeId;
                    case THURSDAY -> thursdayLunch = transformedRecipeId;
                    case FRIDAY -> fridayLunch = transformedRecipeId;
                    case SATURDAY -> saturdayLunch = transformedRecipeId;
                    case SUNDAY -> sundayLunch = transformedRecipeId;
                }
            }
            case DINNER -> {
                switch (dayOfWeek) {
                    case MONDAY -> mondayDinner = transformedRecipeId;
                    case TUESDAY -> tuesdayDinner = transformedRecipeId;
                    case WEDNESDAY -> wednesdayDinner = transformedRecipeId;
                    case THURSDAY -> thursdayDinner = transformedRecipeId;
                    case FRIDAY -> fridayDinner = transformedRecipeId;
                    case SATURDAY -> saturdayDinner = transformedRecipeId;
                    case SUNDAY -> sundayDinner = transformedRecipeId;
                }
            }
        }
    }

    /**
     * 선택된 슬롯만 채우고, 선택되지 않은 슬롯은 null
     */
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

    /**
     * 생성 시 기본 상태를 DRAFT로 시작시키는 팩토리
     */
    public static MealPlan draft(FamilyRoom familyRoom, LocalDate weekStartDate) {
        return MealPlan.builder()
                .familyRoom(familyRoom)
                .weekStartDate(weekStartDate)
                .status(MealPlanStatus.DRAFT)
                .build();
    }
}
