package com.urisik.backend.domain.review.repository;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByFamilyMemberProfileAndRecipe(FamilyMemberProfile profile, Recipe recipe);

}
