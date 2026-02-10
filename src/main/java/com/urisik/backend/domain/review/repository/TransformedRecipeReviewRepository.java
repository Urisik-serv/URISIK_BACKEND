package com.urisik.backend.domain.review.repository;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.review.entity.Review;
import com.urisik.backend.domain.review.entity.TransformedRecipeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransformedRecipeReviewRepository extends JpaRepository<TransformedRecipeReview, Long> {

    boolean existsByFamilyMemberProfileAndTransformedRecipe(FamilyMemberProfile profile, TransformedRecipe transformedRecipe);




    // 1) 조건 만족 reviewId 랜덤 1개
    @Query(value = """
        select rr.id
        from transformed_recipe_review rr
        where rr.family_member_id = :profileId
          and rr.score >= :minScore
        order by rand()
        limit 1
    """, nativeQuery = true)
    Optional<Long> findRandomHighScoreReviewId(@Param("profileId") Long profileId,
                                               @Param("minScore") int minScore);

    // 2) reviewId로 recipe까지 fetch join 해서 가져오기
    @Query("""
        select rv from TransformedRecipeReview rv
        join fetch rv.transformedRecipe
        where rv.id = :id
    """)
    Optional<TransformedRecipeReview> findByIdWithRecipe(@Param("id") Long id);

}
