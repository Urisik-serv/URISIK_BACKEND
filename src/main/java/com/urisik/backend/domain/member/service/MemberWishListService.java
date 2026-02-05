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
import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
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

        // 이미 위시리스트 안에 있는지 검증
        List<Long> recipeIds = req.getRecipeId();
        long existsCount =
                memberWishListRepository.countByFamilyMemberProfile_IdAndRecipe_IdIn(profile.getId(), recipeIds);
        if (existsCount != 0) {
            // 정책: 하나라도 없으면 전체 실패
            throw new MemberException(MemberErrorCode.WISH_ALREADY_IN);
            // 에러코드 새로 만드는 걸 추천: "요청한 위시 중 일부가 존재하지 않음"
        }
        List<Long> transIds = req.getTransformedRecipeId();
        long existsCounts =
                memberTransformedRecipeWishRepository.countByFamilyMemberProfile_IdAndRecipe_IdIn(profile.getId(), transIds);
        if (existsCounts != 0) {
            throw new MemberException(MemberErrorCode.TRANS_WISH_ALREADY_IN);
        }



        // ✅ 추가: 기존 것은 유지하고, 요청으로 들어온 것들을 append
        if(req.getRecipeId() != null) {
            for (Long recipeId : req.getRecipeId()) {
                Recipe recipe = recipeRepository.findById(recipeId)
                        .orElseThrow(() -> new MemberException(MemberErrorCode.NO_RECIPE));//수정 요청 음식 없음

                profile.addWish(MemberWishList.of(recipe));//
                recipe.incrementWishCount();

                // 가족 위시리스트 삭제DB 업데이트
                familyWishListExclusionRepository.deleteByFamilyRoom_IdAndRecipeId(
                        profile.getFamilyRoom().getId(),
                        recipeId
                );
            }
        }
        if(req.getTransformedRecipeId() != null) {
            for (Long recipeId : req.getTransformedRecipeId()) {
                TransformedRecipe recipe = transformedRecipeRepository.findById(recipeId)
                        .orElseThrow(() -> new MemberException(MemberErrorCode.NO_TRANS_RECIPE));//수정 요청 음식 없음

                profile.addTransWish(MemberTransformedRecipeWish.of(recipe));//
                recipe.incrementWishCount();

                // 가족 위시리스트 삭제DB 업데이트
                familyWishListExclusionRepository.deleteByFamilyRoom_IdAndTransformedRecipeId(
                        profile.getFamilyRoom().getId(),
                        recipeId
                );
            }
        }


        return WishListResponse.PostWishes.builder()
                .isPosted(true)
                .build();
    }


    @Transactional
    public WishListResponse.DeleteWishes deleteWishItems
            (Long memberId,Long familyRoomId, WishListRequest.DeleteWishes req) {

        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NO_MEMBER));


        long deleted =0;
        long deletedTrans = 0;
        if(req.getRecipeId() != null) {

            List<Long> recipeIds = req.getRecipeId();
            long existsCount =
                    memberWishListRepository.countByFamilyMemberProfile_IdAndRecipe_IdIn(profile.getId(), recipeIds);
            if (existsCount != recipeIds.size()) {
                // 정책: 하나라도 없으면 전체 실패
                throw new MemberException(MemberErrorCode.WISH_NOT_FOUND);
                // 에러코드 새로 만드는 걸 추천: "요청한 위시 중 일부가 존재하지 않음"
            }

            deleted =
                    memberWishListRepository.deleteByFamilyMemberProfile_IdAndRecipe_IdIn(
                            profile.getId(),
                            req.getRecipeId()
                    );
            memberWishListRepository.decreaseWishCount(req.getRecipeId());
        }
        if(req.getTransformedRecipeId() != null) {

            List<Long> transIds = req.getTransformedRecipeId();
            long existsCount =
                    memberTransformedRecipeWishRepository.countByFamilyMemberProfile_IdAndRecipe_IdIn(profile.getId(), transIds);
            if (existsCount != transIds.size()) {
                throw new MemberException(MemberErrorCode.TRANS_WISH_NOT_FOUND);
            }

            deletedTrans =
                    memberTransformedRecipeWishRepository.deleteByFamilyMemberProfile_IdAndRecipe_IdIn(
                            profile.getId(),
                            req.getTransformedRecipeId()
                    );
            memberTransformedRecipeWishRepository.decreaseWishCount(req.getTransformedRecipeId());
        }
        return WishListResponse.DeleteWishes.builder()
                .isDeleted(true)
                .deletedNum(deleted)
                .deletedTransNum(deletedTrans)
                .build();
    }


    public WishListResponse.GetWishes getMyWishes
            (Long familyRoomId, Long memberId, Long profileId, Long cursor, int size) {

        //1) 검증
        // 요청자가 자신의 프로필을 요청하는 경우
        if (profileId == -1) {
            FamilyMemberProfile mine = familyMemberProfileRepository.findByMember_Id(memberId).orElseThrow(
                    () -> new MemberException(MemberErrorCode.NO_PROFILE_IN_FAMILY)
            );
            profileId = mine.getId();
        }

        // 요청 대상의 프로필이 있는가?
        boolean exists_req_pro = familyMemberProfileRepository.existsById(profileId);
        if (!exists_req_pro) {
            throw new MemberException(MemberErrorCode.NO_PROFILE_IN_FAMILY);
        }

        // 요청자가 방안에 있는지?
        boolean exists = familyMemberProfileRepository.existsByFamilyRoom_IdAndMember_Id(familyRoomId, memberId);
        if (!exists) {
            throw new MemberException(MemberErrorCode.NOT_YOUR_ROOM);
        }

        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndId(familyRoomId,profileId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NO_PROFILE_IN_FAMILY));


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
                        .foodImage(w.getRecipe().getRecipeExternalMetadata().getImageSmallUrl())
                        .category(w.getRecipe().getRecipeExternalMetadata().getCategory())
                        .avgScore(w.getRecipe().getAvgScore())
                        .recipeIngredients(RecipeTextParser.parseIngredients(w.getRecipe().getIngredientsRaw()))
                        .build())
                .toList();

        // 4) 응답
        return WishListResponse.GetWishes.builder()
                .items(items)
                .nextCursor(hasNext ? nextCursor : null)
                .hasNext(hasNext)
                .build();
    }


    public WishListResponse.GetTransWishes getMyTransWishes
            (Long familyRoomId, Long memberId, Long profileId, Long cursor, int size) {

    //1) 검증
        // 요청자가 자신의 프로필을 요청하는 경우
        if (profileId == -1) {
            FamilyMemberProfile mine = familyMemberProfileRepository.findByMember_Id(memberId).orElseThrow(
                    () -> new MemberException(MemberErrorCode.NO_PROFILE_IN_FAMILY)
            );
            profileId = mine.getId();
        }

        // 요청 대상의 프로필이 있는가?
        boolean exists_req_pro = familyMemberProfileRepository.existsById(profileId);
        if (!exists_req_pro) {
            throw new MemberException(MemberErrorCode.NO_PROFILE_IN_FAMILY);
        }

        // 요청자가 방안에 있는지?
        boolean exists = familyMemberProfileRepository.existsByFamilyRoom_IdAndMember_Id(familyRoomId, memberId);
        if (!exists) {
            throw new MemberException(MemberErrorCode.NOT_YOUR_ROOM);
        }

        FamilyMemberProfile profile = familyMemberProfileRepository
                .findByFamilyRoom_IdAndId(familyRoomId,profileId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NO_PROFILE_IN_FAMILY));



        // 2) 커서 페이징 조회 (size+1)
        int limit = Math.min(size, 50);
        PageRequest pageable = PageRequest.of(0, limit + 1);

        List<MemberTransformedRecipeWish> rows = (cursor == null)
                ? memberTransformedRecipeWishRepository.findFirstPage(profile.getId(), pageable)
                : memberTransformedRecipeWishRepository.findNextPage(profile.getId(), cursor, pageable);

        boolean hasNext = rows.size() > limit;
        if (hasNext) rows = rows.subList(0, limit);

        Long nextCursor = rows.isEmpty() ? null : rows.get(rows.size() - 1).getId();

        // 3) DTO 변환 (WishItem 리스트)
        List<WishListResponse.TransWishItem> items = rows.stream()
                .map(w -> WishListResponse.TransWishItem.builder()
                        .wishId(w.getId())
                        .transformedRecipeId(w.getRecipe().getId())
                        .avgScore(w.getRecipe().getAvgScore())
                        .category(w.getRecipe().getBaseRecipe().getRecipeExternalMetadata().getCategory())
                        .foodImage(w.getRecipe().getBaseRecipe().getRecipeExternalMetadata().getImageSmallUrl())
                        .transformedRecipeName(w.getRecipe().getTitle())
                        .recipeIngredients(RecipeTextParser.parseIngredients(w.getRecipe().getIngredientsRaw()))
                        .build())
                .toList();

        // 4) 응답
        return WishListResponse.GetTransWishes.builder()
                .items(items)
                .nextCursor(hasNext ? nextCursor : null)
                .hasNext(hasNext)
                .build();
    }


}
