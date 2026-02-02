package com.urisik.backend.domain.member.service;


import com.urisik.backend.domain.familyroom.repository.FamilyWishListExclusionRepository;
import com.urisik.backend.domain.member.dto.req.WishListRequest;
import com.urisik.backend.domain.member.dto.res.WishListResponse;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.MemberTransformedRecipeWish;
import com.urisik.backend.domain.member.entity.MemberWishList;
import com.urisik.backend.domain.member.exception.MemberException;
import com.urisik.backend.domain.member.exception.code.MemberErrorCode;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.member.repo.MemberTransformedRecipeWishRepository;
import com.urisik.backend.domain.member.repo.MemberWishListRepository;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberWishListService {

    private final RecipeRepository recipeRepository;
    private final MemberWishListRepository memberWishListRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final FamilyWishListExclusionRepository familyWishListExclusionRepository;
    private final TransformedRecipeRepository transformedRecipeRepository;
    private final MemberTransformedRecipeWishRepository memberTransformedRecipeWishRepository;

    @Transactional
    public WishListResponse.PostWishes addWishItems
            (Long memberId, Long familyRoomId, WishListRequest.PostWishes req) {

        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NO_MEMBER));

        // ✅ 추가: 기존 것은 유지하고, 요청으로 들어온 것들을 append
        for (Long recipeId : req.getRecipeId()) {
            Recipe recipe= recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.NO_RECIPE));//수정 요청 음식 없음

            profile.addWish(MemberWishList.of(recipe));//
            recipe.incrementWishCount();

            // 가족 위시리스트 삭제DB 업데이트
            familyWishListExclusionRepository.deleteByFamilyRoom_IdAndRecipeId(
                    profile.getFamilyRoom().getId(),
                    recipeId
            );
        }
        for (Long recipeId : req.getTransformedRecipeId()) {
            TransformedRecipe recipe = transformedRecipeRepository.findById(recipeId)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.NO_RECIPE));//수정 요청 음식 없음

            profile.addTransWish(MemberTransformedRecipeWish.of(recipe));//
            recipe.incrementWishCount();

            // 가족 위시리스트 삭제DB 업데이트
            /*
            familyWishListExclusionRepository.deleteByFamilyRoom_IdAndRecipeId(
                    profile.getFamilyRoom().getId(),
                    recipeId

            );
             */
        }


        return WishListResponse.PostWishes.builder()
                .isSuccess(true)
                .build();
    }


    @Transactional
    public WishListResponse.DeleteWishes deleteWishItems
            (Long memberId,Long familyRoomId, WishListRequest.DeleteWishes req) {

        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NO_MEMBER));

        // req.getRecipeId()가 null/empty면 바로 성공 처리(정책에 따라 에러로 바꿔도 됨)
        if ((req.getRecipeId() == null&&req.getTransformedRecipeId()==null)||
                (req.getRecipeId().isEmpty()&&req.getTransformedRecipeId().isEmpty())) {
            return WishListResponse.DeleteWishes.builder()
                    .isSuccess(true)
                    .build();
        }

        long deleted =
                memberWishListRepository.deleteByFamilyMemberProfile_IdAndRecipe_IdIn(
                profile.getId(),
                req.getRecipeId()
        );
        memberWishListRepository.decreaseWishCount(req.getRecipeId());
        long deletedTrans =
        memberTransformedRecipeWishRepository.deleteByFamilyMemberProfile_IdAndRecipe_IdIn(
                profile.getId(),
                req.getTransformedRecipeId()
        );
        memberTransformedRecipeWishRepository.decreaseWishCount(req.getTransformedRecipeId());

        return WishListResponse.DeleteWishes.builder()
                .isSuccess(true)
                .deletedNum(deleted)
                .deletedTransNum(deletedTrans)
                .build();
    }


    public WishListResponse.GetWishes getMyWishes
            (Long familyRoomId, Long memberId, Long cursor, int size) {

        // 1) 토큰 memberId로 해당 familyRoom 안의 내 프로필 찾기
        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NO_MEMBER));

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
                        .recipeName(w.getRecipe().getTitle())
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
