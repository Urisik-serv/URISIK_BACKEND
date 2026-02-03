package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.MemberWishList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface MemberWishListRepository extends JpaRepository<MemberWishList, Long> {


    @Query("""
    select w from MemberWishList w
    join fetch w.recipe r
    where w.familyMemberProfile.id = :profileId
    order by w.id desc
""")
    List<MemberWishList> findFirstPage(@Param("profileId") Long profileId, Pageable pageable);

    @Query("""
    select w from MemberWishList w
    join fetch w.recipe r
    where w.familyMemberProfile.id = :profileId
      and w.id < :cursor
    order by w.id desc
""")
    List<MemberWishList> findNextPage(@Param("profileId") Long profileId,
                                      @Param("cursor") Long cursor,
                                      Pageable pageable);

    @Query("""
    select w from MemberWishList w
    join fetch w.recipe r
    left join fetch r.recipeExternalMetadata m
    join fetch w.familyMemberProfile p
    where p.familyRoom.id = :familyRoomId
    order by w.id desc
""")
    List<MemberWishList> findAllByFamilyRoomIdWithRecipe(Long familyRoomId);

    @Query("""
    select distinct r.id
    from MemberWishList mw
    join mw.familyMemberProfile p
    join mw.recipe r
    where p.familyRoom.id = :familyRoomId
      and r.id in :recipeIds
""")
    Set<Long> findExistingRecipeIds(Long familyRoomId, List<Long> recipeIds);

    long deleteByFamilyMemberProfile_IdAndRecipe_IdIn(Long profileId, List<Long> recipeIds);

    // ✅ 검증용: 요청 recipeIds 중 내 위시에 존재하는 개수
    long countByFamilyMemberProfile_IdAndRecipe_IdIn(Long profileId, List<Long> recipeIds);

    // 위시리스트 갯수 낮추기
    @Modifying
    @Query("""
        update Recipe r
        set r.wishCount = r.wishCount - 1
        where r.id in :recipeIds
    """)
    int decreaseWishCount(@Param("recipeIds") List<Long> recipeIds);


}
