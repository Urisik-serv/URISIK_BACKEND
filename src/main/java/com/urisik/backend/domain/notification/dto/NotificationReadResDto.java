package com.urisik.backend.domain.notification.dto;

import lombok.Builder;

@Builder
public record NotificationReadResDto(
        boolean isRead
) {
}
