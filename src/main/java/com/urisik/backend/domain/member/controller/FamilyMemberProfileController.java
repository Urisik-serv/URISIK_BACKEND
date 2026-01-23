package com.urisik.backend.domain.member.controller;


import com.urisik.backend.domain.member.dto.req.FamilyMemberProfileRequest;
import com.urisik.backend.domain.member.dto.res.FamilyMemberProfileResponse;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.service.FamilyMemberProfileService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.apiPayload.code.GeneralSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/family-rooms")
public class FamilyMemberProfileController {

    private final FamilyMemberProfileService familyMemberProfileService;

    @PostMapping("/{familyRoomId}/profiles")
    public ApiResponse<FamilyMemberProfileResponse.Create> createProfile(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Object principal,
            @RequestBody @Valid FamilyMemberProfileRequest.Create req
    ) {
        Long memberId = (Long) principal;
        FamilyMemberProfileResponse.Create res= familyMemberProfileService.create(familyRoomId, memberId,req);


        return ApiResponse.onSuccess(GeneralSuccessCode.OK, res);
    }

}
