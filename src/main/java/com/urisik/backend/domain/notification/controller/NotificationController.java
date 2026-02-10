package com.urisik.backend.domain.notification.controller;

import com.urisik.backend.domain.notification.dto.NotificationReadResDto;
import com.urisik.backend.domain.notification.dto.NotificationResDto;
import com.urisik.backend.domain.notification.exception.NotificationSuccessCode;
import com.urisik.backend.domain.notification.service.NotificationService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 1. SSE 실시간 알림 연결
     * @param memberId
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal Long memberId
    ) {
        return notificationService.subscribe(memberId);
    }


    /**
     * 2. 알림 목록 조회
     * @param memberId
     * @param size
     */
    @GetMapping(value = "")
    @Operation(summary = "알림 목록 조회 API", description = "사용자가 받은 알림 목록을 조회합니다. ")
    public ApiResponse<Slice<NotificationResDto>> getNotifications(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size
    ) {
        Slice<NotificationResDto> result = notificationService.getNotifications(memberId, size);
        return ApiResponse.onSuccess(NotificationSuccessCode.NOTIFICATION_GET_SUCCESS, result);
    }


    /**
     * 3. 알림 "읽음" 상태로 변경
     * @param memberId
     */
    @PatchMapping(value = "/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리 API", description = "알림 읽음 상태를 true로 변경합니다. ")
    public ApiResponse<NotificationReadResDto> readNotification(
            @PathVariable(name = "notificationId") Long notificationId,
            @AuthenticationPrincipal Long memberId
            ) {
        return ApiResponse.onSuccess(NotificationSuccessCode.NOTIFICATION_READ_SUCCESS,
                notificationService.readNotification(memberId, notificationId));
    }
}
