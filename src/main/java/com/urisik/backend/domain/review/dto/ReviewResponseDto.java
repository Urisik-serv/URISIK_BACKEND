package com.urisik.backend.domain.review.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReviewResponseDto(
        Long reviewId,
        Double avgScore, // 해당 음식의 평균 별점 (별점 반영 확인용),
        LocalDateTime createdAt // 리뷰 생성 시간
) {
}

