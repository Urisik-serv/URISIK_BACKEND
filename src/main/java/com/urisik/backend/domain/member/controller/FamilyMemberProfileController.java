package com.urisik.backend.domain.member.controller;


import com.urisik.backend.domain.member.dto.req.FamilyMemberProfileRequest;
import com.urisik.backend.domain.member.dto.req.WishListRequest;
import com.urisik.backend.domain.member.dto.res.FamilyMemberProfileResponse;
import com.urisik.backend.domain.member.dto.res.WishListResponse;
import com.urisik.backend.domain.member.exception.code.MemberSuccessCode;
import com.urisik.backend.domain.member.service.FamilyMemberProfileService;
import com.urisik.backend.domain.member.service.MemberWishListService;
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
    private final MemberWishListService memberWishListService;

    @PostMapping("/{familyRoomId}/profiles")
    public ApiResponse<FamilyMemberProfileResponse.Create> createProfile(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid FamilyMemberProfileRequest.Create req
    ) {



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

/*
    ----------------------------------------------------------------------------------------
    wishlist
 */

    @PostMapping("/profile-wishes")
    public ApiResponse<WishListResponse.PostWishes> addWishItems(
            @AuthenticationPrincipal Long loginUserId,
            @RequestBody @Valid WishListRequest.PostWishes req
    ) {
        WishListResponse.PostWishes result = memberWishListService.addWishItems(loginUserId, req);

        return ApiResponse.onSuccess(MemberSuccessCode.MemberProfile_Create,result);
    }

    @GetMapping("/profile-wishes")
    public ApiResponse<WishListResponse.GetWishes> getMyWishes(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Long loginUserId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        WishListResponse.GetWishes result =
                memberWishListService.getMyWishes(familyRoomId, loginUserId, cursor, size);

        return ApiResponse.onSuccess(MemberSuccessCode.MemberProfile_Get, result);
    }
    @DeleteMapping("/profile-wishes")
    public ApiResponse<WishListResponse.DeleteWishes> deleteWishItems(
            @AuthenticationPrincipal Long loginUserId,
            @RequestBody @Valid WishListRequest.DeleteWishes req
    ) {
        WishListResponse.DeleteWishes result = memberWishListService.deleteWishItems(loginUserId, req);

        return ApiResponse.onSuccess(MemberSuccessCode.MemberProfile_Create,result);
    }


}
