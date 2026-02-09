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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "FamilyWishList", description = "가족 위시리스트 관련 API")
public class FamilyWishListController {

    private final FamilyWishListQueryService familyWishListQueryService;
    private static final Logger log = LoggerFactory.getLogger(FamilyWishListController.class);

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

        boolean hasNext = page.hasNext();
        String nextToken = null;

        if (page.nextCursor() != null) {
            nextToken = page.nextCursor().encode();
        }

        // Defensive: if we claim hasNext but cannot provide a cursor, the client cannot fetch next data.
        if (hasNext && (nextToken == null || nextToken.isBlank())) {
            log.warn("Cursor encoding failed while hasNext=true. familyRoomId={}, memberId={}, size={}, cursorPresent={}",
                    familyRoomId, memberId, size, (cursor != null && !cursor.isBlank()));
            hasNext = false;
        }

        headers.add("X-Has-Next", String.valueOf(hasNext));

        if (hasNext && nextToken != null && !nextToken.isBlank()) {
            headers.add("X-Next-Cursor", nextToken);
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
        List<FamilyWishListQueryService.WishItemKey> items = request.items().stream()
                .map(it -> new FamilyWishListQueryService.WishItemKey(it.type(), it.id()))
                .toList();

        familyWishListQueryService.deleteFamilyWishListItems(memberId, familyRoomId, items);
        return ApiResponse.onSuccess(FamilyRoomSuccessCode.FAMILY_WISHLIST_DELETE, null);
    }
}
