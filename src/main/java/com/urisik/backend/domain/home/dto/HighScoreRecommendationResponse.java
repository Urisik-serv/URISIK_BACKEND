package com.urisik.backend.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HighScoreRecommendationResponse {

    private List<HighScoreRecommendationDTO> recipes;

}
