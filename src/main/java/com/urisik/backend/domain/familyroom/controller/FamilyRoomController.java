package com.urisik.backend.domain.familyroom.controller;

import com.urisik.backend.domain.familyroom.dto.req.CreateFamilyRoomReqDTO;
import com.urisik.backend.domain.familyroom.dto.res.CreateFamilyRoomResDTO;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomSuccessCode;
import com.urisik.backend.domain.familyroom.service.FamilyRoomService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FamilyRoomController {

    private final FamilyRoomService familyRoomService;

    @PostMapping("/family-rooms")
    public ApiResponse<CreateFamilyRoomResDTO> createFamilyRoom(
            // 로그인 정책 미정
            // @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody CreateFamilyRoomReqDTO request
    ) {
        // Long memberId = principal.getMemberId();
        Long memberId = 1L;

        CreateFamilyRoomResDTO result = familyRoomService.createFamilyRoom(memberId, request);

        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_ROOM, result);
    }
}
