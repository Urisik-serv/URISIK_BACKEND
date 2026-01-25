package com.urisik.backend.domain.member.controller;


import com.urisik.backend.domain.member.dto.req.FamilyMemberProfileRequest;
import com.urisik.backend.domain.member.dto.res.FamilyMemberProfileResponse;
import com.urisik.backend.domain.member.exception.code.MemberSuccessCode;
import com.urisik.backend.domain.member.service.FamilyMemberProfileService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.apiPayload.code.GeneralSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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


        return ApiResponse.onSuccess(
                MemberSuccessCode.MemberProfile_Create,
                familyMemberProfileService.create(familyRoomId, memberId,req));
    }


    @GetMapping("/{familyRoomId}/profiles")
    public ApiResponse<FamilyMemberProfileResponse.Detail> getMyProfile(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Long loginMemberId
    ) {
        return ApiResponse.onSuccess(
                MemberSuccessCode.MemberProfile_Get,
                familyMemberProfileService.getMyProfile(familyRoomId, loginMemberId)
        );
    }
    @PatchMapping("/{familyRoomId}/profiles")
    public ApiResponse<FamilyMemberProfileResponse.Update> updateMyProfile(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Long loginUserId,
            @RequestBody @Valid FamilyMemberProfileRequest.Update req
    ) {


        FamilyMemberProfileResponse.Update result =
                familyMemberProfileService.update(familyRoomId, loginUserId, req);

        return ApiResponse.onSuccess(MemberSuccessCode.MemberProfile_Update,result);
    }


    @PatchMapping("/{familyRoomId}/profile-pic")
    public ApiResponse<FamilyMemberProfileResponse.UpdatePic> updateMyProfilePicture(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Long loginUserId,
            @RequestBody @Valid FamilyMemberProfileRequest.UpdatePic req
    ){

        FamilyMemberProfileResponse.UpdatePic result =
                familyMemberProfileService.updatePic(familyRoomId, loginUserId, req);

        return ApiResponse.onSuccess(MemberSuccessCode.MemberProfile_Update,result);

    }



    @DeleteMapping("/{familyRoomId}/family-member{profileId}")
    public ApiResponse<FamilyMemberProfileResponse.Delete> deleteFamilyMemberProfile(
            @PathVariable Long familyRoomId,
            @PathVariable Long profileId,
            @AuthenticationPrincipal Long loginUserId
    ) {


        return ApiResponse.onSuccess(MemberSuccessCode.MemberProfile_Delete,
                familyMemberProfileService.quitFamilyRoom(familyRoomId,profileId,loginUserId));
    }






}
