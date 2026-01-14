package com.urisik.backend.domain.familyroom.controller;

import com.urisik.backend.domain.familyroom.dto.req.CreateFamilyRoomReqDTO;
import com.urisik.backend.domain.familyroom.dto.res.CreateFamilyRoomResDTO;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomSuccessCode;
import com.urisik.backend.domain.familyroom.service.FamilyRoomService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family-rooms")
@RequiredArgsConstructor
public class FamilyRoomController {

    private final FamilyRoomService familyRoomService;

    @PostMapping
    public ApiResponse<CreateFamilyRoomResDTO> createFamilyRoom(
            // 로그인 인증 제외
            // @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody CreateFamilyRoomReqDTO request
    ) {
        // Long memberId = principal.getMemberId();
        Long memberId = 1L;

        CreateFamilyRoomResDTO result = familyRoomService.createFamilyRoom(memberId, request);

        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_ROOM, result);
    }
}
