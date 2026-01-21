package com.urisik.backend.domain.member.exception;

import com.urisik.backend.domain.member.exception.code.MemberErrorCode;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;

public class MemberException extends GeneralException {
    public MemberException(MemberErrorCode code) {
        super(code);
    }
}
