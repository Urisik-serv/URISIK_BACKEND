package com.urisik.backend.global.auth.exception;

import com.urisik.backend.global.apiPayload.exception.GeneralException;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;


public class AuthenExcetion extends GeneralException {

    public AuthenExcetion(AuthErrorCode code) {super(code);}

}
