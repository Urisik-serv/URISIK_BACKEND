package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.MemberTransformedRecipeWish;
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



}
