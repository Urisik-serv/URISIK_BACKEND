package com.urisik.backend.domain.home.candidate;

public interface HomeRecipeCandidate {

    Long getId();
    String getTitle();

    String getImageSmallUrl();
    String getImageLargeUrl();
    String getCategory();

    double getAvgScore();
    int getReviewCount();

    int getWishCount();
    String getIngredientsRaw();

}
