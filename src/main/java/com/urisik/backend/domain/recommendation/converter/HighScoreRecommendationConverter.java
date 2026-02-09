package com.urisik.backend.domain.recommendation.converter;

import com.urisik.backend.domain.recommendation.candidate.HighScoreRecipeCandidate;
import com.urisik.backend.domain.recommendation.candidate.TransformedRecipeCandidateLow;
import com.urisik.backend.domain.recommendation.dto.HighScoreRecommendationDTO;
import org.springframework.stereotype.Component;

@Component
public class HighScoreRecommendationConverter {

    public HighScoreRecommendationDTO toDto(HighScoreRecipeCandidate candidate, boolean isSafe) {

        boolean isTransformed =
                candidate instanceof TransformedRecipeCandidateLow;

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
