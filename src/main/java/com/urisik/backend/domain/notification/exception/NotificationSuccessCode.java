package com.urisik.backend.domain.notification.exception;

import com.urisik.backend.global.apiPayload.code.BaseSuccessCode;
import com.urisik.backend.global.apiPayload.code.SuccessReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum NotificationSuccessCode implements BaseSuccessCode {

    NOTIFICATION_SUBSCRIBE_SUCCESS(HttpStatus.OK,
            "NOTI_200_1",
            "실시간 알림 연결 성공"),

     NOTIFICATION_GET_SUCCESS(HttpStatus.OK,
            "NOTI_200_2",
            "알림 목록 조회 성공");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public SuccessReason getReason() {
        return SuccessReason.builder()
                .httpStatus(httpStatus)
                .code(code)
                .message(message)
                .build();
    }
}
