package com.urisik.backend.domain.familyroom.service;

import com.urisik.backend.domain.familyroom.dto.res.FamilyWishListItemResDTO;
import com.urisik.backend.domain.familyroom.entity.FamilyWishList;
import com.urisik.backend.domain.familyroom.enums.FamilyPolicy;
import com.urisik.backend.domain.familyroom.exception.FamilyRoomException;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomErrorCode;
import com.urisik.backend.domain.familyroom.repository.FamilyWishListRepository;
import com.urisik.backend.domain.member.enums.FamilyRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyWishListQueryService {

    private final FamilyWishListRepository familyWishListRepository;
    private final FamilyRoomService familyRoomService;

    /**
     * 가족 위시리스트 조회
     * TODO(리팩터링):
     * - 개인 위시리스트 연동 시:
     *   1) 개인 위시리스트 집계
     *   2) FamilyWishList와 교집합
     *   3) sourceProfile 실제 구성
     * - NewFood 연동 시:
     *   foodId → 음식 상세 매핑
     */
    public List<FamilyWishListItemResDTO> getFamilyWishList(Long familyRoomId) {

        // 1. FamilyWishList 기준으로 foodId 목록 조회
        List<FamilyWishList> familyWishLists =
                familyWishListRepository.findAllByFamilyRoomId(familyRoomId);

        // 2. foodId별로 더미 응답 생성
        return familyWishLists.stream()
                .map(this::toMockResponse)
                .collect(Collectors.toList());
    }

    /**
     * 가족 위시리스트 항목 삭제 (방장만 가능)
     * - 방장 권한은 FamilyPolicy + FamilyRole 조합으로 계산
     * - 방장 삭제는 가족 위시리스트(FamilyWishList)에서만 제거
     */
    @Transactional
    public void deleteFamilyWishListItems(
            Long memberId,
            Long familyRoomId,
            List<Long> foodIds
    ) {
        validateLeader(memberId, familyRoomId);

        if (foodIds == null || foodIds.isEmpty()) {
            return;
        }

        familyWishListRepository.deleteByFamilyRoomIdAndFoodIdIn(familyRoomId, foodIds);
    }

    /**
     * 방장 검증
     * 방장 판단 기준:
     * - FamilyPolicy.isLeaderRole(FamilyRole)
     */
    private void validateLeader(Long memberId, Long familyRoomId) {

        FamilyPolicy policy =
                familyRoomService.getFamilyPolicy(familyRoomId);

        FamilyRole myRole =
                familyRoomService.getMyFamilyRole(memberId, familyRoomId);

        if (!policy.isLeaderRole(myRole)) {
            throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_WISHLIST_FORBIDDEN);
        }
    }

    /**
     * 임시 Mock 응답 생성
     * TODO(리팩터링):
     * - NewFood 연동 시 실제 데이터 매핑
     * - 개인 위시리스트 연동 시 sourceProfile 실제 구성
     */
    private FamilyWishListItemResDTO toMockResponse(FamilyWishList familyWishList) {

        Long foodId = familyWishList.getFoodId();

        return new FamilyWishListItemResDTO(
                foodId,
                "임시 음식 이름 " + foodId,
                "https://cdn.example.com/foods/" + foodId + "/thumbnail.jpg",
                4.5,
                new FamilyWishListItemResDTO.FoodCategory(
                        "TEMP",
                        "임시 카테고리"
                ),
                new FamilyWishListItemResDTO.SourceProfile(
                        List.of(
                                new FamilyWishListItemResDTO.Profile(1L, "엄마"),
                                new FamilyWishListItemResDTO.Profile(2L, "아빠")
                        )
                )
        );
    }
}
