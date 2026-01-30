package com.urisik.backend.domain.review.controller;

import com.urisik.backend.domain.review.dto.ReviewRequestDto;
import com.urisik.backend.domain.review.dto.ReviewResponseDto;
import com.urisik.backend.domain.review.exception.ReviewSuccessCode;
import com.urisik.backend.domain.review.service.TransformedReviewService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transformed-recipes")
@Tag(name = "Review", description = "리뷰 관련 API")
public class TransformedReviewController {

    private final TransformedReviewService reviewService;

    @PostMapping("/{transformedRecipeId}/reviews")
    @Operation(
            summary = "변형 레시피 간단 후기 작성 API",
            description = "변형 레시피에 대해 별점, 취향 여부를 등록합니다."
    )
    public ApiResponse<ReviewResponseDto> createReview(
            @PathVariable Long transformedRecipeId,
            @RequestBody ReviewRequestDto request,
            @AuthenticationPrincipal Long memberId
    ) {
        ReviewResponseDto response =
                reviewService.createReview(request, memberId, transformedRecipeId);

        return ApiResponse.onSuccess(
                ReviewSuccessCode.CREATE_REVIEW_OK,
                response
        );
    }
}

