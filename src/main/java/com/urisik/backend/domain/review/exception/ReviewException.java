package com.urisik.backend.domain.review.exception;

import com.urisik.backend.global.apiPayload.exception.GeneralException;

public class ReviewException extends GeneralException {
    public ReviewException(ReviewErrorCode code) {
        super(code);
    }
}
