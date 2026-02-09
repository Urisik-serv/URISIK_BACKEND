package com.urisik.backend.domain.recommendation.dto.res;

import com.urisik.backend.domain.recommendation.dto.HomeSafeRecipeDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HomeSafeRecipeResponseDTO {

    private List<HomeSafeRecipeDTO> recipes;

}
