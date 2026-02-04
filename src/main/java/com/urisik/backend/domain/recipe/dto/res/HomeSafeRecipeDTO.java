package com.urisik.backend.domain.recipe.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HomeSafeRecipeDTO {

    private String id;
    private String title;
    private String imageUrl;
    private String category;
    private double avgScore;
    private int reviewCount;
    private int wishCount;
    private boolean isSafe;

}

