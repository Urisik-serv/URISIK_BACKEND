package com.urisik.backend.domain.recommendation.dto.res;

import com.urisik.backend.domain.recommendation.dto.HomeSafeRecommendationRecipeDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HomeSafeRecipeResponseDTO {

    private List<HomeSafeRecommendationRecipeDTO> recipes;

}
