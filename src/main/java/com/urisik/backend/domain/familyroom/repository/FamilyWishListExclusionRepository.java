package com.urisik.backend.domain.familyroom.repository;

import com.urisik.backend.domain.familyroom.entity.FamilyWishListExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface FamilyWishListExclusionRepository extends JpaRepository<FamilyWishListExclusion, Long>, FamilyWishListExclusionRepositoryCustom {

    /**
     * 해당 가족방에서 제외된 recipeId 목록
     */
    @Query("""
        select e.recipeId
        from FamilyWishListExclusion e
        where e.familyRoom.id = :familyRoomId
    """)
    Set<Long> findExcludedRecipeIdsByFamilyRoomId(Long familyRoomId);

    /**
     * 개인이 다시 담았을 때 exclusion 해제
     */
    void deleteByFamilyRoom_IdAndRecipeId(Long familyRoomId, Long recipeId);

    /**
     * 필요 시 bulk 해제용
     */
    void deleteByFamilyRoom_IdAndRecipeIdIn(Long familyRoomId, List<Long> recipeIds);
}
