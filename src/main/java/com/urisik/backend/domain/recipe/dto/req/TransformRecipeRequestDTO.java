package com.urisik.backend.domain.recipe.dto.req;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.recipe.enums.Visibility;
import lombok.Getter;

import java.util.List;

@Getter
public class TransformRecipeRequestDTO {

    private List<Allergen> allergens;
    private Visibility visibility;

}

