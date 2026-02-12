package com.urisik.backend.global.external.s3.exception;

import com.urisik.backend.global.apiPayload.code.BaseSuccessCode;
import com.urisik.backend.global.apiPayload.code.SuccessReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum S3SuccessCode implements BaseSuccessCode {

    S3_UPLOAD_SUCCESSFUL(HttpStatus.OK, "S3_200_1", "사진 업로드 성공"),
    S3_REMOVE_SUCCESSFUL(HttpStatus.OK, "S3_200_2", "사진 삭제 성공");

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
