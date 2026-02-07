package com.urisik.backend.domain.home.candidate;

import com.urisik.backend.domain.home.policy.CategoryMapper;
import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.recipe.entity.Recipe;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class RecipeCandidateLow implements HighScoreRecipeCandidate{

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
                ? CategoryMapper.map(
                recipe.getRecipeExternalMetadata().getCategory()
        )
                : CategoryMapper.map(null);
    }

    @Override public double getAvgScore() { return recipe.getAvgScore(); }
    @Override public int getReviewCount() { return recipe.getReviewCount(); }
    @Override public int getWishCount() { return recipe.getWishCount(); }

    @Override
    public List<String> getIngredients() {
        return RecipeTextParser.parseIngredients(
                recipe.getIngredientsRaw()
        );
    }

    @Override
    public String getDescription() {
        return String.join(", ", getIngredients());
    }

}
