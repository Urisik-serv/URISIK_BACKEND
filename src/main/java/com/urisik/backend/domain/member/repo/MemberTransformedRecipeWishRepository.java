package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.MemberTransformedRecipeWish;
import com.urisik.backend.domain.member.entity.MemberWishList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberTransformedRecipeWishRepository extends JpaRepository<MemberTransformedRecipeWish, Long> {


    long deleteByFamilyMemberProfile_IdAndRecipe_IdIn(Long profileId, List<Long> recipeIds);
    @Modifying
    @Query("""
        update Recipe r
        set r.wishCount = r.wishCount - 1
        where r.id in :recipeIds
    """)
    int decreaseWishCount(@Param("recipeIds") List<Long> recipeIds);

    //존재 검증용
    long countByFamilyMemberProfile_IdAndRecipe_IdIn(Long profileId, List<Long> recipeIds);


    @Query("""
    select w from MemberWishList w
    join fetch w.recipe r
    where w.familyMemberProfile.id = :profileId
    order by w.id desc
""")
    List<MemberTransformedRecipeWish> findFirstPage(@Param("profileId") Long profileId, Pageable pageable);

    @Query("""
    select w from MemberWishList w
    join fetch w.recipe r
    where w.familyMemberProfile.id = :profileId
      and w.id < :cursor
    order by w.id desc
""")
    List<MemberTransformedRecipeWish> findNextPage(@Param("profileId") Long profileId,
                                      @Param("cursor") Long cursor,
                                      Pageable pageable);


}
