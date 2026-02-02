package com.urisik.backend.domain.familyroom.repository;

import java.util.List;

public interface FamilyWishListExclusionRepositoryCustom {

    /**
     * 방장이 가족 위시리스트에서 제외(삭제)한 원형 레시피 항목을 exclusion으로 기록
     * - (familyRoomId, recipeId) 유니크이므로 중복 삽입 방지 필요
     */
    void excludeRecipes(Long familyRoomId, List<Long> recipeIds);

    /**
     * 방장이 가족 위시리스트에서 제외(삭제)한 변형 레시피 항목을 exclusion으로 기록
     * - (familyRoomId, transformedRecipeId) 유니크이므로 중복 삽입 방지 필요
     */
    void excludeTransformedRecipes(Long familyRoomId, List<Long> transformedRecipeIds);
}
