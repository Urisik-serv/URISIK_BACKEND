package com.urisik.backend.domain.familyroom.controller;

import com.urisik.backend.domain.familyroom.dto.res.AcceptInviteResDTO;
import com.urisik.backend.domain.familyroom.dto.res.CreateInviteResDTO;
import com.urisik.backend.domain.familyroom.dto.res.ReadInviteResDTO;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomSuccessCode;
import com.urisik.backend.domain.familyroom.service.InviteService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Invite", description = "초대 관련 API")
public class InviteController {

    private final InviteService inviteService;

    /**
     * 초대 토큰 생성 API
     */
    @PostMapping("/family-rooms/{familyRoomId}/invites")
    public ApiResponse<CreateInviteResDTO> createInvite(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Long memberId
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.Token_Not_Vaild);
        }

        CreateInviteResDTO result = inviteService.createInvite(familyRoomId, memberId);
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.INVITE_CREATED, result);
    }

    /**
     * 초대 토큰 조회 API (비로그인 허용)
     */
    @GetMapping("/invites/{token}")
    public ApiResponse<ReadInviteResDTO> readInvite(
            @PathVariable String token
    ) {
        ReadInviteResDTO result = inviteService.readInvite(token);
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.INVITE_PREVIEW, result);
    }

    /**
     * 초대 토큰 수락 API
     */
    @PostMapping("/invites/{token}/accept")
    public ApiResponse<AcceptInviteResDTO> acceptInvite(
            @PathVariable String token,
            @AuthenticationPrincipal Long memberId
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.Token_Not_Vaild);
        }

        AcceptInviteResDTO result = inviteService.acceptInvite(token, memberId);
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_JOIN, result);
    }
}
