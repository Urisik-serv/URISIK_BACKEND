package com.urisik.backend.domain.recommendation.converter;

import com.urisik.backend.domain.recommendation.candidate.HighScoreRecommendationRecipeCandidate;
import com.urisik.backend.domain.recommendation.candidate.RecommendationTransformedRecipeCandidateLow;
import com.urisik.backend.domain.recommendation.dto.HighScoreRecommendationDTO;
import org.springframework.stereotype.Component;

@Component
public class HighScoreRecommendationConverter {

    public HighScoreRecommendationDTO toDto(HighScoreRecommendationRecipeCandidate candidate, boolean isSafe) {

        boolean isTransformed =
                candidate instanceof RecommendationTransformedRecipeCandidateLow;

        return new HighScoreRecommendationDTO(
                candidate.getId().toString(),
                candidate.getTitle(),
                candidate.getImageUrl(),
                candidate.getCategory(),
                candidate.getAvgScore(),
                isSafe,
                candidate.getDescription(),
                candidate.getReviewCount(),
                candidate.getWishCount(),
                isTransformed
        );
    }
}
