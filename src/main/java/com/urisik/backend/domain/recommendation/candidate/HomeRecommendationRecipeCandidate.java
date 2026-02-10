package com.urisik.backend.domain.recommendation.candidate;

import java.util.List;

public interface HomeRecommendationRecipeCandidate {

    Long getId();
    String getTitle();
    String getImageUrl();
    List<String> getIngredients();
    String getDescription();
    int getWishCount();

    String getCategory();
    double getAvgScore();
    int getReviewCount();

}
