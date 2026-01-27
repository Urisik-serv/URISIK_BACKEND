package com.urisik.backend.domain.member.service;


import com.urisik.backend.domain.familyroom.repository.FamilyWishListExclusionRepository;
import com.urisik.backend.domain.member.dto.req.WishListRequest;
import com.urisik.backend.domain.member.dto.res.WishListResponse;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.MemberWishList;
import com.urisik.backend.domain.member.exception.MemberException;
import com.urisik.backend.domain.member.exception.code.MemberErrorCode;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.member.repo.MemberWishListRepository;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberWishListService {

    RecipeRepository recipeRepository;
    MemberWishListRepository memberWishListRepository;
    FamilyMemberProfileRepository familyMemberProfileRepository;
    private final FamilyWishListExclusionRepository familyWishListExclusionRepository;

    @Transactional
    public WishListResponse.PostWishes addWishItems
            (Long loginUserId, WishListRequest.PostWishes req) {


        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByMember_Id(loginUserId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Member));

        // ✅ 추가: 기존 것은 유지하고, 요청으로 들어온 것들을 append
        for (Long recipeId : req.getRecipeId()) {
            Recipe recipe= recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.No_Member));//수정

            profile.addWish(MemberWishList.of(recipe));

            familyWishListExclusionRepository.deleteByFamilyRoom_IdAndRecipeId(
                    profile.getFamilyRoom().getId(),
                    recipeId
            );
        }



        return WishListResponse.PostWishes.builder()
                .isSuccess(true)
                .build();
    }


    @Transactional
    public WishListResponse.DeleteWishes deleteWishItems
            (Long loginUserId, WishListRequest.DeleteWishes req) {

        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByMember_Id(loginUserId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Member));

        // req.getRecipeId()가 null/empty면 바로 성공 처리(정책에 따라 에러로 바꿔도 됨)
        if (req.getRecipeId() == null || req.getRecipeId().isEmpty()) {
            return WishListResponse.DeleteWishes.builder()
                    .isSuccess(true)
                    .build();
        }

        long deleted =
                memberWishListRepository.deleteByFamilyMemberProfile_IdAndRecipe_IdIn(
                profile.getId(),
                req.getRecipeId()
        );

        return WishListResponse.DeleteWishes.builder()
                .isSuccess(true)
                .deletedNum(deleted)
                .build();
    }


    public WishListResponse.GetWishes getMyWishes(Long familyRoomId, Long memberId, Long cursor, int size) {

        // 1) 토큰 memberId로 해당 familyRoom 안의 내 프로필 찾기
        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.No_Member));

        // 2) 커서 페이징 조회 (size+1)
        int limit = Math.min(size, 50);
        PageRequest pageable = PageRequest.of(0, limit + 1);

        List<MemberWishList> rows = (cursor == null)
                ? memberWishListRepository.findFirstPage(profile.getId(), pageable)
                : memberWishListRepository.findNextPage(profile.getId(), cursor, pageable);

        boolean hasNext = rows.size() > limit;
        if (hasNext) rows = rows.subList(0, limit);

        Long nextCursor = rows.isEmpty() ? null : rows.get(rows.size() - 1).getId();

        // 3) DTO 변환 (WishItem 리스트)
        List<WishListResponse.WishItem> items = rows.stream()
                .map(w -> WishListResponse.WishItem.builder()
                        .wishId(w.getId())
                        .recipeId(w.getRecipe().getId())
                        .recipeName(w.getRecipe().getName())
                        .build())
                .toList();

        // 4) 응답
        return WishListResponse.GetWishes.builder()
                .isSuccess(true)
                .items(items)
                .nextCursor(hasNext ? nextCursor : null)
                .hasNext(hasNext)
                .build();
    }


}
