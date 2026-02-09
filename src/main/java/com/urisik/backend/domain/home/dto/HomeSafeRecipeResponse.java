package com.urisik.backend.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HomeSafeRecipeResponse {

    private List<HomeSafeRecipeDTO> recipes;

}
