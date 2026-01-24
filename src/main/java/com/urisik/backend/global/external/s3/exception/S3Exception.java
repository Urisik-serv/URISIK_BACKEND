package com.urisik.backend.global.external.s3.exception;

import com.urisik.backend.global.apiPayload.exception.GeneralException;

public class S3Exception extends GeneralException {
    public S3Exception(S3ErrorCode code) {
        super(code);
    }
}
