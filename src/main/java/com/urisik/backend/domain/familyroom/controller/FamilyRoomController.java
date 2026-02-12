package com.urisik.backend.domain.familyroom.controller;

import com.urisik.backend.domain.familyroom.dto.req.CreateFamilyRoomReqDTO;
import com.urisik.backend.domain.familyroom.dto.res.CreateFamilyRoomResDTO;
import com.urisik.backend.domain.familyroom.dto.res.ReadFamilyRoomContextResDTO;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomSuccessCode;
import com.urisik.backend.domain.familyroom.service.FamilyRoomService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "FamilyRoom", description = "가족방 관련 API")
public class FamilyRoomController {

    private final FamilyRoomService familyRoomService;

    @PostMapping("/family-rooms")
    @Operation(summary = "가족방 생성 API")
    public ApiResponse<CreateFamilyRoomResDTO> createFamilyRoom(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CreateFamilyRoomReqDTO request
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }

        CreateFamilyRoomResDTO result = familyRoomService.createFamilyRoom(memberId, request);
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_ROOM, result);
    }

    @GetMapping("/family-rooms/me")
    @Operation(summary = "가족방 컨텍스트 조회 API")
    public ApiResponse<ReadFamilyRoomContextResDTO> readMyFamilyRoomContext(
            @AuthenticationPrincipal Long memberId
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }

        ReadFamilyRoomContextResDTO result = familyRoomService.readMyFamilyRoomContext(memberId);
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_ROOM_CONTEXT, result);
    }
}
