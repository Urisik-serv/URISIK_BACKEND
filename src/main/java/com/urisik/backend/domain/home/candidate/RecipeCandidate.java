package com.urisik.backend.domain.home.candidate;

import com.urisik.backend.domain.recipe.entity.Recipe;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecipeCandidate implements HomeRecipeCandidate {

    private final Recipe recipe;

    @Override public Long getId() { return recipe.getId(); }
    @Override public String getTitle() { return recipe.getTitle(); }

    @Override
    public String getImageUrl() {
        return recipe.getRecipeExternalMetadata() != null
                ? recipe.getRecipeExternalMetadata().getImageLargeUrl()
                : null;
    }

    @Override
    public String getCategory() {
        return recipe.getRecipeExternalMetadata() != null
                ? recipe.getRecipeExternalMetadata().getCategory()
                : null;
    }

    @Override public double getAvgScore() { return recipe.getAvgScore(); }
    @Override public int getReviewCount() { return recipe.getReviewCount(); }
    @Override public int getWishCount() { return recipe.getWishCount(); }

    @Override
    public String getIngredientsRaw() {
        return recipe.getIngredientsRaw();
    }

}

