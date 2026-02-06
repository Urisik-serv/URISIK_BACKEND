package com.urisik.backend.domain.home.candidate;

import java.util.List;

public interface HighScoreRecipeCandidate {

    Long getId();
    String getTitle();
    String getImageUrl();
    String getCategory();

    double getAvgScore();
    int getReviewCount();
    int getWishCount();

    List<String> getIngredients();
}

