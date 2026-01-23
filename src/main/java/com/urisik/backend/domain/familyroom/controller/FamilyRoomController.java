package com.urisik.backend.domain.familyroom.controller;

import com.urisik.backend.domain.familyroom.dto.req.CreateFamilyRoomReqDTO;
import com.urisik.backend.domain.familyroom.dto.res.CreateFamilyRoomResDTO;
import com.urisik.backend.domain.familyroom.dto.res.ReadFamilyRoomContextResDTO;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomSuccessCode;
import com.urisik.backend.domain.familyroom.service.FamilyRoomService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FamilyRoomController {

    private final FamilyRoomService familyRoomService;

    @PostMapping("/family-rooms")
    public ApiResponse<CreateFamilyRoomResDTO> createFamilyRoom(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CreateFamilyRoomReqDTO request
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.Token_Not_Vaild);
        }

        CreateFamilyRoomResDTO result = familyRoomService.createFamilyRoom(memberId, request);
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_ROOM, result);
    }

    @GetMapping("/family-rooms/me")
    public ApiResponse<ReadFamilyRoomContextResDTO> readMyFamilyRoomContext(
            @AuthenticationPrincipal Long memberId
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.Token_Not_Vaild);
        }

        ReadFamilyRoomContextResDTO result = familyRoomService.readMyFamilyRoomContext(memberId);
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_ROOM_CONTEXT, result);
    }
}
