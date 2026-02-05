package com.urisik.backend.domain.notification.converter;

import com.urisik.backend.domain.notification.dto.NotificationResDto;
import com.urisik.backend.domain.notification.entity.Notification;
import org.springframework.data.domain.Slice;


public class NotificationConverter {

    // 알림 목록 조회
    public static Slice<NotificationResDto> toNotificationResponseListDto(Slice<Notification> notiList){
        return notiList.map(NotificationConverter::toNotificationResponseDto);

    }

    public static NotificationResDto toNotificationResponseDto(Notification notification) {
        return NotificationResDto.builder()
                .isRead(notification.isRead())
                .type(notification.getType())
                .createdAt(notification.getCreateAt())
                .mealPlanGenerationCount(notification.getMealPlanGenerationCount())
                .build();
    }


}
