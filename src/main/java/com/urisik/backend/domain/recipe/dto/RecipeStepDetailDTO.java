package com.urisik.backend.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecipeStepDetailDTO {

    private int order;
    private String description;
    private String imageUrl;

}

