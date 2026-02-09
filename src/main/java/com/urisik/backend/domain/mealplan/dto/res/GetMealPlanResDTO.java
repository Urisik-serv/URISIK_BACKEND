package com.urisik.backend.domain.mealplan.dto.res;

import com.urisik.backend.domain.mealplan.enums.MealType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class GetMealPlanResDTO {

    /** 슬롯에 저장된 선택 타입(원형/변형) */
    public enum SlotRefType {
        RECIPE,
        TRANSFORMED_RECIPE
    }

    /** 오늘의 식단 조회 응답 */
    public record TodayMealPlanResDTO(
            LocalDate date,
            Long mealPlanId,
            LocalDate weekStartDate,
            List<TodayMealDTO> meals
    ) {}

    public record TodayMealDTO(
            MealType mealType,
            SlotRefType type,         // RECIPE / TRANSFORMED_RECIPE
            Long id,                  // 선택된 엔티티의 id (type에 따라 Recipe.id 또는 TransformedRecipe.id)
            String title,
            String imageUrl,          // RecipeExternalMetadata#getThumbnailImageUrl() (imageSmallUrl 우선). 없으면 null
            String ingredients,
            List<MealPlanRecipeStepDTO> recipeSteps
    ) {}

    /** 레시피 조리 단계 (순서 + 설명 + 이미지) */
    public record MealPlanRecipeStepDTO(
            int stepOrder,
            String description,
            String imageUrl
    ) {}

    /** 이번주 식단 조회 응답 */
    public record WeeklyMealPlanResDTO(
            Long mealPlanId,
            LocalDate weekStartDate,
            Map<String, SlotSummaryDTO> slots
    ) {}

    public record SlotSummaryDTO(
            SlotRefType type,         // RECIPE / TRANSFORMED_RECIPE
            Long id,                  // 선택된 엔티티의 id (type에 따라 Recipe.id 또는 TransformedRecipe.id)
            String title,
            String imageUrl,          // RecipeExternalMetadata#getThumbnailImageUrl() (imageSmallUrl 우선). 없으면 null
            String ingredients
    ) {}

    /** 기간별 식단 기록 조회 응답 (fromDate ~ toDate) */
    public record MealPlanHistoryResDTO(
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
            SlotRefType type,         // RECIPE / TRANSFORMED_RECIPE
            Long id,                  // 선택된 엔티티의 id (type에 따라 Recipe.id 또는 TransformedRecipe.id)
            String title,
            String imageUrl,          // RecipeExternalMetadata#getThumbnailImageUrl() (imageSmallUrl 우선). 없으면 null
            String ingredients
    ) {}
}
