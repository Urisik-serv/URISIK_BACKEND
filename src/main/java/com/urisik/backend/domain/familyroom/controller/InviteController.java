package com.urisik.backend.domain.familyroom.controller;

import com.urisik.backend.domain.familyroom.dto.res.CreateInviteResDTO;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomSuccessCode;
import com.urisik.backend.domain.familyroom.service.InviteService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/family-rooms")
public class InviteController {

    private final InviteService inviteService;

    @PostMapping("/{familyRoomId}/invites")
    public ApiResponse<CreateInviteResDTO> createInvite(
            @PathVariable Long familyRoomId
            // 로그인 정책 미정
            // @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Long memberId = userDetails.getMemberId();
        Long memberId = 1L;
        CreateInviteResDTO result = inviteService.createInvite(familyRoomId, memberId);

        return ApiResponse.onSuccess(FamilyRoomSuccessCode.INVITE_CREATED, result);
    }
}
