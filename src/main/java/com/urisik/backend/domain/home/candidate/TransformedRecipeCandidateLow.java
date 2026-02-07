package com.urisik.backend.domain.home.candidate;

import com.urisik.backend.domain.home.policy.CategoryMapper;
import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TransformedRecipeCandidateLow implements HighScoreRecipeCandidate{

    private final TransformedRecipe tr;

    @Override public Long getId() { return tr.getId(); }
    @Override public String getTitle() { return tr.getTitle(); }

    @Override
    public String getImageUrl() {
        return tr.getBaseRecipe().getRecipeExternalMetadata() != null
                ? tr.getBaseRecipe().getRecipeExternalMetadata().getImageLargeUrl()
                : null;
    }

    @Override
    public String getCategory() {
        return tr.getBaseRecipe().getRecipeExternalMetadata() != null
                ? CategoryMapper.map(
                tr.getBaseRecipe()
                        .getRecipeExternalMetadata()
                        .getCategory()
        )
                : CategoryMapper.map(null);
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
    public List<String> getIngredients() {
        return RecipeTextParser.parseIngredients(
                tr.getIngredientsRaw()
        );
    }

    @Override
    public String getDescription() {
        return tr.getSubstitutionSummaryJson();
    }

}
