package com.urisik.backend.domain.home.converter;

import com.urisik.backend.domain.home.candidate.HomeRecipeCandidate;
import com.urisik.backend.domain.home.candidate.TransformedRecipeCandidate;
import com.urisik.backend.domain.home.dto.HomeSafeRecipeDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
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


