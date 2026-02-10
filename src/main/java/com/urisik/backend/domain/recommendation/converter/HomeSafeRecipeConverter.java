package com.urisik.backend.domain.recommendation.converter;

import com.urisik.backend.domain.recommendation.candidate.HomeRecommendationRecipeCandidate;
import com.urisik.backend.domain.recommendation.candidate.RecommendationTransformedRecipeCandidate;
import com.urisik.backend.domain.recommendation.dto.HomeSafeRecommendationRecipeDTO;
import org.springframework.stereotype.Component;

@Component
public class HomeSafeRecipeConverter {

    public HomeSafeRecommendationRecipeDTO toDto(HomeRecommendationRecipeCandidate c) {

        boolean isTransformed =
                c instanceof RecommendationTransformedRecipeCandidate;

        return new HomeSafeRecommendationRecipeDTO(
                c.getId().toString(),
                c.getTitle(),
                c.getImageUrl(),
                c.getDescription(),
                c.getWishCount(),
                c.getCategory(),
                c.getAvgScore(),
                c.getReviewCount(),
                isTransformed,
                true
        );
    }
}


