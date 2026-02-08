package com.urisik.backend.domain.review.controller;

import com.urisik.backend.domain.review.dto.ReviewRequestDto;
import com.urisik.backend.domain.review.dto.ReviewResponseDto;
import com.urisik.backend.domain.review.exception.ReviewSuccessCode;
import com.urisik.backend.domain.review.service.ReviewService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
@Tag(name = "Review", description = "리뷰 관련 API")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 1. 특정 레시피에 대한 간단 후기 작성 API
     */
    @PostMapping(value = "/{recipeId}/reviews")
    @Operation(summary = "간단 후기 작성 API", description = "특정 메뉴에 대해 별점, 취향 여부 정보를 입력하는 api 입니다. ")
    public ApiResponse<ReviewResponseDto> createReview(
            @PathVariable(name = "recipeId") Long recipe_id,
            @RequestBody @Valid ReviewRequestDto request,
            @AuthenticationPrincipal Long memberId
            ) {

        ReviewResponseDto responseDto = reviewService.createReview(request, memberId, recipe_id);
        return ApiResponse.onSuccess(ReviewSuccessCode.CREATE_REVIEW_OK, responseDto);
    }
}
