package com.urisik.backend.domain.notification.exception;

import com.urisik.backend.global.apiPayload.exception.GeneralException;

public class NotificationException extends GeneralException {
    public NotificationException(NotificationErrorCode code) {
        super(code);
    }
}
