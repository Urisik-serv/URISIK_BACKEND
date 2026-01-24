package com.urisik.backend.domain.recipe.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecipeStep {

    private int order;
    private String description;
    private String imageUrl;

}

