package com.urisik.backend.domain.review.dto;

import lombok.Builder;

@Builder
public record ReviewResponseDto(
        Long reviewId,
        Double avgScore // 해당 음식의 평균 별점 (별점 반영 확인용)
) {
}
