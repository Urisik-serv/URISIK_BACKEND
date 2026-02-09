package com.urisik.backend.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HomeSafeRecipeDTO {

    private String id;
    private String title;
    private String imageUrl;
    private String description;
    private int wishCount;

    private String category;
    private double avgScore;
    private int reviewCount;
    private boolean isTransformed;
    private boolean isSafe;

}

