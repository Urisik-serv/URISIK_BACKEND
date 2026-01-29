package com.urisik.backend.domain.review.repository;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.review.entity.Review;
import com.urisik.backend.domain.review.entity.TransformedRecipeReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransformedRecipeReviewRepository extends JpaRepository<TransformedRecipeReview, Long> {

    boolean existsByFamilyMemberProfileAndTransformedRecipe(FamilyMemberProfile profile, TransformedRecipe transformedRecipe);

}
