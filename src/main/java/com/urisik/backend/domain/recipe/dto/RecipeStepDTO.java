package com.urisik.backend.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecipeStepDTO {

    private int order;
    private String description;

}