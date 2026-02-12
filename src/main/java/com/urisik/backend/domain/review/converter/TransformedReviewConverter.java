package com.urisik.backend.domain.review.converter;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.review.dto.ReviewRequestDto;
import com.urisik.backend.domain.review.dto.ReviewResponseDto;
import com.urisik.backend.domain.review.entity.TransformedRecipeReview;

public class TransformedReviewConverter {

    // req -> TransformedRecipeReview
    public static TransformedRecipeReview toReview(
            FamilyMemberProfile familyMember,
            TransformedRecipe recipe,
            ReviewRequestDto request
    ) {
        return TransformedRecipeReview.builder()
                .score(request.score())
                .isFavorite(request.isFavorite())
                .familyMemberProfile(familyMember)
                .transformedRecipe(recipe)
                .build();
    }

    // Review -> res
    public static ReviewResponseDto toReviewResponseDto(TransformedRecipeReview transReview, Double avgScore) {
        return ReviewResponseDto.builder()
                .reviewId(transReview.getId())
                .avgScore(avgScore)
                .createdAt(transReview.getCreateAt())
                .build();
    }
}

