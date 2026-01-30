package com.urisik.backend.domain.mealplan.controller;

import com.urisik.backend.domain.mealplan.dto.req.CreateMealPlanReqDTO;
import com.urisik.backend.domain.mealplan.dto.req.UpdateMealPlanReqDTO;
import com.urisik.backend.domain.mealplan.dto.res.CreateMealPlanResDTO;
import com.urisik.backend.domain.mealplan.dto.res.UpdateMealPlanResDTO;
import com.urisik.backend.domain.mealplan.exception.code.MealPlanSuccessCode;
import com.urisik.backend.domain.mealplan.service.MealPlanService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family-rooms/{familyRoomId}/meal-plans")
@Tag(name = "MealPlan", description = "식단 관련 API")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @PostMapping("")
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
}
