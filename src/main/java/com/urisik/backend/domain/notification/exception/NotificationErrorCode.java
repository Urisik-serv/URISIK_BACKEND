package com.urisik.backend.domain.notification.exception;

import com.urisik.backend.global.apiPayload.code.BaseErrorCode;
import com.urisik.backend.global.apiPayload.code.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {

    NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "NOTI_500_1",
            "실시간 알림 전송 중 오류가 발생했습니다."),

    NOTIFICATION_SUBSCRIBE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "NOTI_500_2",
            "SSE 구독 연결 중 오류가 발생했습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReason getReason() {
        return ErrorReason.builder()
                .httpStatus(httpStatus)
                .code(code)
                .message(message)
                .build();
    }
}
