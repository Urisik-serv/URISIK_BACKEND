package com.urisik.backend.domain.review.converter;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.review.dto.ReviewRequestDto;
import com.urisik.backend.domain.review.dto.ReviewResponseDto;
import com.urisik.backend.domain.review.entity.Review;

public class ReviewConverter {

    // req -> Review
    public static Review toReview(FamilyMemberProfile familyMember, Recipe recipe, ReviewRequestDto request) {
        return Review.builder()
                .score(request.score())
                .isFavorite(request.isFavorite())
                .familyMemberProfile(familyMember)
                .recipe(recipe)
                .build();
    }


    // Review -> res
    public static ReviewResponseDto toReviewResponseDto(Review review, Double avgScore) {
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .avgScore(avgScore)
                .createdAt(review.getCreateAt())
                .build();
    }

}
