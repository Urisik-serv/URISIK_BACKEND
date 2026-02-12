package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.MemberTransformedRecipeWish;
import com.urisik.backend.domain.member.entity.MemberWishList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface MemberTransformedRecipeWishRepository extends JpaRepository<MemberTransformedRecipeWish, Long> {


    long deleteByFamilyMemberProfile_IdAndRecipe_IdIn(Long profileId, List<Long> recipeIds);
    @Modifying
    @Query("""
    update TransformedRecipe tr
    set tr.wishCount = tr.wishCount - 1
    where tr.id in :recipeIds
""")
    int decreaseWishCount(@Param("recipeIds") List<Long> recipeIds);

    //존재 검증용
    long countByFamilyMemberProfile_IdAndRecipe_IdIn(Long profileId, List<Long> recipeIds);


    @Query("""
    select w from MemberTransformedRecipeWish w
    join fetch w.recipe r
    join fetch r.baseRecipe br
    left join fetch br.recipeExternalMetadata m
    where w.familyMemberProfile.id = :profileId
    order by w.id desc
""")
    List<MemberTransformedRecipeWish> findFirstPage(@Param("profileId") Long profileId, Pageable pageable);

    @Query("""
    select w from MemberTransformedRecipeWish w
    join fetch w.recipe r
    join fetch r.baseRecipe br
    left join fetch br.recipeExternalMetadata m
    where w.familyMemberProfile.id = :profileId
      and w.id < :cursor
    order by w.id desc
""")
    List<MemberTransformedRecipeWish> findNextPage(@Param("profileId") Long profileId,
                                      @Param("cursor") Long cursor,
                                      Pageable pageable);

    @Query("""
            select distinct w
            from MemberTransformedRecipeWish w
            join fetch w.familyMemberProfile p
            join fetch w.recipe r
            join p.familyRoom fr
            where fr.id = :familyRoomId
            """)
    List<MemberTransformedRecipeWish> findAllByFamilyRoomIdWithRecipe(
            @Param("familyRoomId") Long familyRoomId
    );

    @Query("""
    select distinct tr.id
    from MemberTransformedRecipeWish mw
    join mw.familyMemberProfile p
    join mw.recipe tr
    where p.familyRoom.id = :familyRoomId
      and tr.id in :transformedIds
""")
    Set<Long> findExistingTransformedRecipeIds(Long familyRoomId, List<Long> transformedIds);


    @EntityGraph(attributePaths = {"recipe"})
    List<MemberTransformedRecipeWish> findMemberWishListsByFamilyMemberProfile_Id(Long familyMemberProfileId);


}
