package com.urisik.backend.domain.recipe.converter;

import com.urisik.backend.domain.recipe.dto.res.HomeSafeRecipeDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import org.springframework.stereotype.Component;

@Component
public class HomeSafeRecipeConverter {

    public HomeSafeRecipeDTO toDto(Recipe recipe) {

        RecipeExternalMetadata meta = recipe.getRecipeExternalMetadata();

        return new HomeSafeRecipeDTO(
                recipe.getId().toString(),
                recipe.getTitle(),
                meta != null ? meta.getThumbnailImageUrl() : null,
                meta != null ? meta.getCategory() : null,
                recipe.getAvgScore(),
                recipe.getReviewCount(),
                recipe.getWishCount(),
                true
        );
    }
}

