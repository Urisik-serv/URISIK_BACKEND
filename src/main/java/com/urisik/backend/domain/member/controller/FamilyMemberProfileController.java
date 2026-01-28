package com.urisik.backend.domain.member.controller;


import com.urisik.backend.domain.member.dto.req.FamilyMemberProfileRequest;
import com.urisik.backend.domain.member.dto.req.WishListRequest;
import com.urisik.backend.domain.member.dto.res.FamilyMemberProfileResponse;
import com.urisik.backend.domain.member.dto.res.WishListResponse;
import com.urisik.backend.domain.member.exception.code.MemberSuccessCode;
import com.urisik.backend.domain.member.service.FamilyMemberProfileService;
import com.urisik.backend.domain.member.service.MemberWishListService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/family-rooms")
@Tag(name = "Profiles", description = "프로필 관련 API")
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
                MemberSuccessCode.MEMBER_PROFILE_CREATE,
                familyMemberProfileService.create(familyRoomId, memberId,req));
    }



    @GetMapping("/{familyRoomId}/profiles")
    public ApiResponse<FamilyMemberProfileResponse.Detail> getMyProfile(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.onSuccess(
                MemberSuccessCode.MEMBER_PROFILE_GET,
                familyMemberProfileService.getMyProfile(familyRoomId, memberId)
        );
    }
    @PatchMapping("/{familyRoomId}/profiles")
    public ApiResponse<FamilyMemberProfileResponse.Update> updateMyProfile(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid FamilyMemberProfileRequest.Update req
    ) {


        FamilyMemberProfileResponse.Update result =
                familyMemberProfileService.update(familyRoomId, memberId, req);

        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_PROFILE_UPDATE,result);
    }


    @PatchMapping(value = "/{familyRoomId}/profile-pic", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FamilyMemberProfileResponse.UpdatePic> updateMyProfilePicture(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Long memberId,
            @RequestPart("file") MultipartFile file
    ){
        var result = familyMemberProfileService.updatePic(familyRoomId, memberId, file);
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_PROFILE_PIC_UPDATE, result);
    }


    @DeleteMapping("/{familyRoomId}/profiles/{profileId}")
    public ApiResponse<FamilyMemberProfileResponse.Delete> deleteFamilyMemberProfile(
            @PathVariable Long familyRoomId,
            @PathVariable Long profileId,
            @AuthenticationPrincipal Long memberId
    ) {


        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_PROFILE_DELETE,
                familyMemberProfileService.quitFamilyRoom(familyRoomId,profileId,memberId));
    }

/*
    ----------------------------------------------------------------------------------------
    wishlist
 */

    @PostMapping("/{familyRoomId}/profile-wishes")
    public ApiResponse<WishListResponse.PostWishes> addWishItems(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid WishListRequest.PostWishes req
    ) {
        WishListResponse.PostWishes result = memberWishListService.addWishItems(memberId,familyRoomId, req);

        return ApiResponse.onSuccess(MemberSuccessCode.WISH_LIST_CREATE,result);
    }

    @GetMapping("/{familyRoomId}/profile-wishes")
    public ApiResponse<WishListResponse.GetWishes> getMyWishes(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        WishListResponse.GetWishes result =
                memberWishListService.getMyWishes(familyRoomId, memberId, cursor, size);

        return ApiResponse.onSuccess(MemberSuccessCode.WISH_LIST_GET, result);
    }
    @DeleteMapping("/{familyRoomId}/profile-wishes")
    public ApiResponse<WishListResponse.DeleteWishes> deleteWishItems(
            @PathVariable Long familyRoomId,
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid WishListRequest.DeleteWishes req
    ) {
        WishListResponse.DeleteWishes result = memberWishListService.deleteWishItems(memberId,familyRoomId, req);

        return ApiResponse.onSuccess(MemberSuccessCode.WISH_LIST_DELETE,result);
    }


}
