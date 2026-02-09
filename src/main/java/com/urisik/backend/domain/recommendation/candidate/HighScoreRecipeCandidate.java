package com.urisik.backend.domain.recommendation.candidate;

import java.util.List;

public interface HighScoreRecipeCandidate {

    Long getId();
    String getTitle();
    String getImageUrl();
    String getCategory();
    double getAvgScore();
    List<String> getIngredients();
    String getDescription();

    int getReviewCount();
    int getWishCount();

}

