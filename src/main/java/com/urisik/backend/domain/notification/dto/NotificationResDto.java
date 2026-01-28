package com.urisik.backend.domain.notification.dto;

import com.urisik.backend.domain.notification.enums.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationResDto (
        Boolean isRead,
        NotificationType type,
        LocalDateTime createdAt
        // Long mealPlanCount <- 추후 식단 생성 횟수 카운트 로직 생성 완료 시 추가 예정
) {
}
