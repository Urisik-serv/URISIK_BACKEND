package com.urisik.backend.domain.mealplan.dto.res;

import com.urisik.backend.domain.mealplan.enums.MealType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class GetMealPlanResDTO {

    /** 오늘의 식단 조회 응답 */
    public record TodayMealPlanResDTO(
            LocalDate date,
            Long mealPlanId,
            LocalDate weekStartDate,
            List<TodayMealDTO> meals
    ) {}

    public record TodayMealDTO(
            MealType mealType,
            Long recipeId,
            Long transformedRecipeId, // 없으면 null
            String title,
            String imageUrl,          // 현재 Recipe 엔티티에 없으면 null
            String ingredients,
            String instructions
    ) {}

    /** 이번주 식단 조회 응답 */
    public record WeeklyMealPlanResDTO(
            Long mealPlanId,
            LocalDate weekStartDate,
            Map<String, SlotSummaryDTO> slots
    ) {}

    public record SlotSummaryDTO(
            Long recipeId,
            Long transformedRecipeId, // 없으면 null
            String title,
            String imageUrl,          // 현재 Recipe 엔티티에 없으면 null
            String description,
            String ingredients
    ) {}

    /** 최근 1개월 식단 조회 응답 */
    public record MonthlyMealPlanResDTO(
            LocalDate fromDate,
            LocalDate toDate,
            List<WeekHistoryDTO> weeks
    ) {}

    public record WeekHistoryDTO(
            Long mealPlanId,
            LocalDate weekStartDate,
            List<DayHistoryDTO> days
    ) {}

    public record DayHistoryDTO(
            DayOfWeek dayOfWeek,
            List<HistoryMealDTO> meals
    ) {}

    public record HistoryMealDTO(
            MealType mealType,
            Long recipeId,
            Long transformedRecipeId,
            String title,
            String imageUrl,          // 현재 Recipe 엔티티에 없으면 null
            String description,
            String ingredients
    ) {}
}
