package com.urisik.backend.domain.familyroom.service;

import com.urisik.backend.domain.familyroom.dto.res.FamilyWishListItemResDTO;
import com.urisik.backend.domain.familyroom.repository.FamilyWishListExclusionRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.MemberWishList;
import com.urisik.backend.domain.member.repo.MemberWishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FamilyWishListQueryService {

    private final MemberWishListRepository memberWishListRepository;
    private final FamilyWishListExclusionRepository familyWishListExclusionRepository;
    private final FamilyRoomService familyRoomService;

    /**
     * 가족 위시리스트 조회
     * - 개인 위시리스트(MemberWishList)를 가족방 단위로 집계
     * - 방장이 삭제한 항목은 exclusion에 의해 조회에서만 제외
     */
    // TODO(리팩터링): Recipe 알레르기 성분 + 가족 구성원 알레르기 집계로 usableForMealPlan 계산 로직 추가
    public List<FamilyWishListItemResDTO> getFamilyWishList(Long familyRoomId) {

        // 방장 제외 목록 조회 (exclusion)
        Set<Long> excludedRecipeIds = familyWishListExclusionRepository.findExcludedRecipeIdsByFamilyRoomId(familyRoomId);

        // 가족방 내 전체 개인 위시리스트 조회 (recipe + profile join fetch)
        List<MemberWishList> all = memberWishListRepository.findAllByFamilyRoomIdWithRecipe(familyRoomId);
        if (all.isEmpty()) return List.of();

        // exclusion 반영 + recipeId 기준 그룹핑
        // all 쿼리는 w.id desc로 내려온다고 가정 -> LinkedHashMap 유지하면 최신순 정렬 유지됨
        Map<Long, List<MemberWishList>> grouped = new LinkedHashMap<>();

        for (MemberWishList w : all) {
            Long recipeId = w.getRecipe().getId();

            if (excludedRecipeIds.contains(recipeId)) continue;

            grouped.computeIfAbsent(recipeId, k -> new ArrayList<>()).add(w);
        }

        if (grouped.isEmpty()) return List.of();

        /**
         * 정렬
         * - 개인 위시리스트에 담은 인원 수 많은 순 (DESC)
         * - 최신순 (MemberWishList.id 기준 DESC)
         */
        List<Map.Entry<Long, List<MemberWishList>>> sortedEntries =
                new ArrayList<>(grouped.entrySet());

        sortedEntries.sort((e1, e2) -> {
            // 인원 수 많은 순
            int cmp = Integer.compare(e2.getValue().size(), e1.getValue().size());
            if (cmp != 0) return cmp;

            // 최신순 (각 그룹 내 가장 최신 wish id 기준)
            Long latest1 = e1.getValue().get(0).getId();
            Long latest2 = e2.getValue().get(0).getId();
            return Long.compare(latest2, latest1);
        });

        // DTO 변환
        List<FamilyWishListItemResDTO> result = new ArrayList<>();

        for (Map.Entry<Long, List<MemberWishList>> entry : sortedEntries) {
            Long recipeId = entry.getKey();
            List<MemberWishList> rows = entry.getValue();

            // 같은 recipeId 그룹에서는 recipe 정보 동일
            String recipeName = rows.get(0).getRecipe().getTitle();

            // 누가 위시했는지
            Map<Long, FamilyWishListItemResDTO.Profile> uniqueProfiles = new LinkedHashMap<>();
            for (MemberWishList w : rows) {
                FamilyMemberProfile p = w.getFamilyMemberProfile();

                uniqueProfiles.putIfAbsent(
                        p.getId(),
                        new FamilyWishListItemResDTO.Profile(p.getId(), p.getNickname())
                );
            }

            boolean usableForMealPlan = true;

            result.add(new FamilyWishListItemResDTO(
                    recipeId,
                    recipeName,
                    "https://cdn.example.com/foods/" + recipeId + "/thumbnail.jpg",
                    4.5,
                    new FamilyWishListItemResDTO.FoodCategory("TEMP", "임시 카테고리"),
                    usableForMealPlan,
                    new FamilyWishListItemResDTO.SourceProfile(new ArrayList<>(uniqueProfiles.values()))
            ));
        }

        return result;
    }

    /**
     * 방장만 가족 위시리스트 항목 삭제 (=exclusion 등록)
     * - 개인이 해당 항목을 삭제했다가 다시 담으면 exclusion 해제 (로직은 개인 위시리스트 add 시점에서 수행)
     */
    public void deleteFamilyWishListItems(Long memberId, Long familyRoomId, List<Long> recipeId) {

        // 방장 검증
        familyRoomService.validateLeader(memberId, familyRoomId);

        if (recipeId == null || recipeId.isEmpty()) {
            return;
        }

        familyWishListExclusionRepository.excludeRecipes(familyRoomId, recipeId);
    }
}
