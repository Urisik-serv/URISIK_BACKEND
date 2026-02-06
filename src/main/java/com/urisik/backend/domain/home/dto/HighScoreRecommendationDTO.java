package com.urisik.backend.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HighScoreRecommendationDTO {

    private String id;
    private String title;
    private String imageUrl;
    private String category;

    private double avgScore;
    private int reviewCount;
    private int wishCount;

    private boolean isTransformed;

}
