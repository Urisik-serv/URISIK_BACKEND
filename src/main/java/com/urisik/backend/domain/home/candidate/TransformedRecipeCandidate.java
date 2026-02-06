package com.urisik.backend.domain.home.candidate;

import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransformedRecipeCandidate implements HomeRecipeCandidate {

    private final TransformedRecipe tr;

    @Override public Long getId() { return tr.getId(); }
    @Override public String getTitle() { return tr.getTitle(); }

    @Override
    public String getImageSmallUrl() {
        return tr.getBaseRecipe().getRecipeExternalMetadata() != null
                ? tr.getBaseRecipe().getRecipeExternalMetadata().getImageSmallUrl()
                : null;
    }

    @Override
    public String getImageLargeUrl() {
        return tr.getBaseRecipe().getRecipeExternalMetadata() != null
                ? tr.getBaseRecipe().getRecipeExternalMetadata().getImageLargeUrl()
                : null;
    }

    @Override
    public String getCategory() {
        return tr.getBaseRecipe().getRecipeExternalMetadata() != null
                ? tr.getBaseRecipe().getRecipeExternalMetadata().getCategory()
                : null;
    }

    @Override public double getAvgScore() {
        return tr.getBaseRecipe().getAvgScore();
    }

    @Override public int getReviewCount() {
        return tr.getBaseRecipe().getReviewCount();
    }

    @Override public int getWishCount() {
        return tr.getWishCount();
    }

    @Override
    public String getIngredientsRaw() {
        return tr.getIngredientsRaw();
    }
}

