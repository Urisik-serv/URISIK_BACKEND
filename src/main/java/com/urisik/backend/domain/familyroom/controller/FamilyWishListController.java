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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<List<FamilyWishListItemResDTO>>> readFamilyWishList(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long familyRoomId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (memberId == null) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }

        // Base64(JSON) 커서 디코딩 (유효하지 않으면 null → 첫 페이지처럼 동작)
        FamilyWishListQueryService.Cursor decodedCursor =
                FamilyWishListQueryService.Cursor.decode(cursor);

        FamilyWishListQueryService.PageResult page =
                familyWishListQueryService.getFamilyWishList(memberId, familyRoomId, decodedCursor, size);

        ApiResponse<List<FamilyWishListItemResDTO>> body =
                ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_WISHLIST, page.items());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Has-Next", String.valueOf(page.hasNext()));

        if (page.nextCursor() != null) {
            String next = page.nextCursor().encode();
            if (next != null) {
                headers.add("X-Next-Cursor", next);
            }
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
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
        familyWishListQueryService.deleteFamilyWishListItems(memberId, familyRoomId, request.recipeId(), request.transformedRecipeId());
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_WISHLIST_DELETE, null);
    }
}
