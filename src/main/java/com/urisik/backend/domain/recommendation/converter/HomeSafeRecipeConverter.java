package com.urisik.backend.domain.recommendation.converter;

import com.urisik.backend.domain.recommendation.candidate.HomeRecipeCandidate;
import com.urisik.backend.domain.recommendation.candidate.TransformedRecipeCandidate;
import com.urisik.backend.domain.recommendation.dto.HomeSafeRecipeDTO;
import org.springframework.stereotype.Component;

@Component
public class HomeSafeRecipeConverter {

    public HomeSafeRecipeDTO toDto(HomeRecipeCandidate c) {

        boolean isTransformed =
                c instanceof TransformedRecipeCandidate;

        return new HomeSafeRecipeDTO(
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


