package com.urisik.backend.domain.mealplan.entity;

import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.mealplan.enums.MealPlanStatus;
import com.urisik.backend.domain.mealplan.enums.MealType;
import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "meal_plan",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_meal_plan_family_room_week_start", columnNames = {"family_room_id", "week_start_date"})
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
    private java.time.LocalDate weekStartDate;

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

    public Long getSlot(MealType mealType, java.time.DayOfWeek dayOfWeek) {
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

    public void setSlot(MealType mealType, java.time.DayOfWeek dayOfWeek, Long foodId) {
        switch (mealType) {
            case LUNCH -> {
                switch (dayOfWeek) {
                    case MONDAY -> mondayLunch = foodId;
                    case TUESDAY -> tuesdayLunch = foodId;
                    case WEDNESDAY -> wednesdayLunch = foodId;
                    case THURSDAY -> thursdayLunch = foodId;
                    case FRIDAY -> fridayLunch = foodId;
                    case SATURDAY -> saturdayLunch = foodId;
                    case SUNDAY -> sundayLunch = foodId;
                }
            }
            case DINNER -> {
                switch (dayOfWeek) {
                    case MONDAY -> mondayDinner = foodId;
                    case TUESDAY -> tuesdayDinner = foodId;
                    case WEDNESDAY -> wednesdayDinner = foodId;
                    case THURSDAY -> thursdayDinner = foodId;
                    case FRIDAY -> fridayDinner = foodId;
                    case SATURDAY -> saturdayDinner = foodId;
                    case SUNDAY -> sundayDinner = foodId;
                }
            }
        }
    }
}
