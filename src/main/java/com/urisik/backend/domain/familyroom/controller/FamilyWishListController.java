package com.urisik.backend.domain.familyroom.controller;

import com.urisik.backend.domain.familyroom.dto.req.DeleteFamilyWishListReqDTO;
import com.urisik.backend.domain.familyroom.dto.res.FamilyWishListItemResDTO;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomSuccessCode;
import com.urisik.backend.domain.familyroom.service.FamilyWishListQueryService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FamilyWishListController {

    private final FamilyWishListQueryService familyWishListQueryService;

    /**
     * 가족 위시리스트 조회
     */
    @GetMapping("/family-rooms/{familyRoomId}/family-wishlist")
    public ApiResponse<List<FamilyWishListItemResDTO>> readFamilyWishList(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long familyRoomId
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }

        List<FamilyWishListItemResDTO> result = familyWishListQueryService.getFamilyWishList(familyRoomId);
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_WISHLIST, result);
    }

    /**
     * 가족 위시리스트 항목 삭제 (방장만 가능)
     * - FamilyWishList(물리화 테이블)에서만 제거한다. (개인 위시리스트 X)
     */
    @DeleteMapping("/family-rooms/{familyRoomId}/family-wishlist/items")
    public ApiResponse<Void> deleteFamilyWishListItems(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long familyRoomId,
            @Valid @RequestBody DeleteFamilyWishListReqDTO request
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }

        familyWishListQueryService.deleteFamilyWishListItems(memberId, familyRoomId, request.newFoodIds());
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_WISHLIST, null);
    }
}
