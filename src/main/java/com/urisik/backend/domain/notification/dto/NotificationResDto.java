package com.urisik.backend.domain.notification.dto;

import com.urisik.backend.domain.notification.enums.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationResDto (
        Long id,
        Boolean isRead,
        NotificationType type,
        LocalDateTime createdAt,
        Integer mealPlanGenerationCount
) {
}
