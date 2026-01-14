package com.urisik.backend.domain.familyroom.exception;

import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;

public class FamilyRoomException extends GeneralException {
    public FamilyRoomException(FamilyRoomErrorCode code) {
        super(code);
    }
}
