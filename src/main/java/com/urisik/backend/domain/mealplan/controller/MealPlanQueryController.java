package com.urisik.backend.domain.mealplan.controller;

import com.urisik.backend.domain.mealplan.dto.res.GetMealPlanResDTO;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanSuccessCode;
import com.urisik.backend.domain.mealplan.service.MealPlanQueryService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/family-rooms/{familyRoomId}/meal-plans")
@Tag(name = "MealPlan", description = "주간 식단 관련 API")
public class MealPlanQueryController {

    private final MealPlanQueryService mealPlanQueryService;

    @GetMapping("/today")
    @Operation(summary = "오늘의 식단 조회 API")
    public ApiResponse<GetMealPlanResDTO.TodayMealPlanResDTO> getTodayMealPlan(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long familyRoomId
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }

        GetMealPlanResDTO.TodayMealPlanResDTO result =
                mealPlanQueryService.getTodayMealPlan(memberId, familyRoomId);

        return ApiResponse.onSuccess(MealPlanSuccessCode.MEAL_PLAN_GET, result);
    }

    @GetMapping("/this-week")
    @Operation(summary = "이번주 식단 조회 API")
    public ApiResponse<GetMealPlanResDTO.WeeklyMealPlanResDTO> getThisWeekMealPlan(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long familyRoomId,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }

        GetMealPlanResDTO.WeeklyMealPlanResDTO result =
                mealPlanQueryService.getThisWeekMealPlan(memberId, familyRoomId, date);

        return ApiResponse.onSuccess(MealPlanSuccessCode.MEAL_PLAN_GET, result);
    }

    @GetMapping("/last-month")
    @Operation(summary = "최근 1개월 식단 조회 API")
    public ApiResponse<GetMealPlanResDTO.MonthlyMealPlanResDTO> getLastMonthHistory(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long familyRoomId
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }

        GetMealPlanResDTO.MonthlyMealPlanResDTO result =
                mealPlanQueryService.getLastMonthMealPlan(memberId, familyRoomId);

        return ApiResponse.onSuccess(MealPlanSuccessCode.MEAL_PLAN_GET, result);
    }
}
