package com.urisik.backend.global.apiPayload.code;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode extends BaseCode {

    HttpStatus getHttpStatus();
    ErrorReason getReason();

}
