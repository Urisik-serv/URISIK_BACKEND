package com.urisik.backend.domain.mealplan.controller;

import com.urisik.backend.domain.mealplan.dto.req.CreateMealPlanReqDTO;
import com.urisik.backend.domain.mealplan.dto.req.UpdateMealPlanReqDTO;
import com.urisik.backend.domain.mealplan.dto.res.ConfirmMealPlanResDTO;
import com.urisik.backend.domain.mealplan.dto.res.CreateMealPlanResDTO;
import com.urisik.backend.domain.mealplan.dto.res.UpdateMealPlanResDTO;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanSuccessCode;
import com.urisik.backend.domain.mealplan.service.MealPlanService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/family-rooms/{familyRoomId}/meal-plans")
@Tag(name = "MealPlan", description = "주간 식단 관련 API")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @PostMapping("")
    @Operation(summary = "주간 식단 생성 API")
    public ApiResponse<CreateMealPlanResDTO> createWeeklyMealPlan(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long familyRoomId,
            @Valid @RequestBody CreateMealPlanReqDTO request
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }

        CreateMealPlanResDTO result = mealPlanService.createMealPlan(memberId, familyRoomId, request);

        if (request.regenerate()) {
            return ApiResponse.onSuccess(MealPlanSuccessCode.MEAL_PLAN_REGENERATED, result);
        }
        return ApiResponse.onSuccess(MealPlanSuccessCode.MEAL_PLAN_CREATED, result);
    }

    @PatchMapping("/{mealPlanId}")
    @Operation(summary = "주간 식단 수정 API")
    public ApiResponse<UpdateMealPlanResDTO> updateMealPlan(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long familyRoomId,
            @PathVariable Long mealPlanId,
            @Valid @RequestBody UpdateMealPlanReqDTO request
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }

        UpdateMealPlanResDTO result =
                mealPlanService.updateMealPlan(memberId, familyRoomId, mealPlanId, request);

        return ApiResponse.onSuccess(MealPlanSuccessCode.MEAL_PLAN_UPDATED, result);
    }

    @PostMapping("/{mealPlanId}/confirm")
    @Operation(summary = "주간 식단 확정 API")
    public ApiResponse<ConfirmMealPlanResDTO> confirmMealPlan(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long familyRoomId,
            @PathVariable Long mealPlanId
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }

        ConfirmMealPlanResDTO result = mealPlanService.confirmMealPlan(memberId, familyRoomId, mealPlanId);
        return ApiResponse.onSuccess(MealPlanSuccessCode.MEAL_PLAN_CONFIRMED, result);
    }
}
