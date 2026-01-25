package com.urisik.backend.domain.member.service;


import com.urisik.backend.domain.member.dto.req.WishListRequest;
import com.urisik.backend.domain.member.dto.res.WishListResponse;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.MemberWishList;
import com.urisik.backend.domain.member.exception.MemberException;
import com.urisik.backend.domain.member.exception.code.MemberErrorCode;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberWishListService {

    FamilyMemberProfileRepository familyMemberProfileRepository;

    public WishListResponse.PostWishes addWishItems
            (Long loginUserId, WishListRequest.PostWishes req) {


        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByMember_Id(loginUserId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Member));

        // ✅ 추가: 기존 것은 유지하고, 요청으로 들어온 것들을 append
        for (String foodName : req.getWishItems()) {
            profile.addWish(MemberWishList.of(foodName));
        }

        // 응답용: 현재 wish 목록 문자열로 반환
        List<String> wishItems = profile.getMemberWishLists().stream()
                .map(MemberWishList::getFoodName)
                .toList();

        return WishListResponse.PostWishes.builder()
                .isSuccess(true)
                .wishItems(wishItems)
                .build();
    }

    public WishListResponse.GetWishes getMyWishes(Long familyRoomId, Long memberId, Long cursor, int size) {

        // 1) 토큰 memberId로 해당 familyRoom 안의 내 프로필 찾기
        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Member));

        // 2) 커서 페이징 조회 (size+1로 가져와서 hasNext 판단)
        int limit = Math.min(size, 50); // 방어적으로 상한
        PageRequest pageable = PageRequest.of(0, limit + 1);

        /*
        List<MemberWishList> rows = (cursor == null)
                ? memberWishListRepository.findFirstPage(profile.getId(), pageable)
                : memberWishListRepository.findNextPage(profile.getId(), cursor, pageable);

        boolean hasNext = rows.size() > limit;
        if (hasNext) rows = rows.subList(0, limit);

        Long nextCursor = rows.isEmpty() ? null : rows.get(rows.size() - 1).getId();

        // 3) DTO 변환
        List<WishResponse.WishItem> items = rows.stream()
                .map(w -> WishResponse.WishItem.builder()
                        .id(w.getId())
                        .foodName(w.getFoodName())
                        .build())
                .toList();


         */
        return WishListResponse.GetWishes.builder()
                .isSuccess(true)
                .nextCursor(hasNext ? nextCursor : null)
                .hasNext(hasNext)
                .build();
    }


}
