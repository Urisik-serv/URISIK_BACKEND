package com.urisik.backend.domain.familyroom.controller;

import com.urisik.backend.domain.familyroom.dto.req.DeleteFamilyWishListReqDTO;
import com.urisik.backend.domain.familyroom.dto.res.FamilyWishListItemResDTO;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomSuccessCode;
import com.urisik.backend.domain.familyroom.service.FamilyWishListQueryService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "FamilyWishList", description = "가족 위시리스트 관련 API")
public class FamilyWishListController {

    private final FamilyWishListQueryService familyWishListQueryService;

    @GetMapping("/family-rooms/{familyRoomId}/family-wishlist")
    @Operation(summary = "가족 위시리스트 조회 API")
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

    @DeleteMapping("/family-rooms/{familyRoomId}/family-wishlist/items")
    @Operation(summary = "가족 위시리스트 항목 삭제 API")
    public ApiResponse<Void> deleteFamilyWishListItems(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long familyRoomId,
            @Valid @RequestBody DeleteFamilyWishListReqDTO request
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }
        familyWishListQueryService.deleteFamilyWishListItems(memberId, familyRoomId, request.recipeIds());
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_WISHLIST_DELETE, null);
    }
}
