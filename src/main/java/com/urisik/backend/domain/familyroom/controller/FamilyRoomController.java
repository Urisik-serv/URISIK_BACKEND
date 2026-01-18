package com.urisik.backend.domain.familyroom.controller;

import com.urisik.backend.domain.familyroom.dto.req.CreateFamilyRoomReqDTO;
import com.urisik.backend.domain.familyroom.dto.res.CreateFamilyRoomResDTO;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomSuccessCode;
import com.urisik.backend.domain.familyroom.service.FamilyRoomService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FamilyRoomController {

    private final FamilyRoomService familyRoomService;
    private static final Long TEMP_MEMBER_ID = 1L;

    @PostMapping("/family-rooms")
    public ApiResponse<CreateFamilyRoomResDTO> createFamilyRoom(
            // 로그인 정책 미정
            // @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateFamilyRoomReqDTO request
    ) {
        // Long memberId = principal.getMemberId();
        Long memberId = TEMP_MEMBER_ID;
        CreateFamilyRoomResDTO result = familyRoomService.createFamilyRoom(memberId, request);
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_ROOM, result);
    }
}
