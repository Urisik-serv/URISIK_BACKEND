package com.urisik.backend.domain.recipe.infrastructure.external.foodsafety;

import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.dto.FoodSafetyRecipeResponse;

public interface FoodSafetyRecipeClient {

    FoodSafetyRecipeResponse.Row fetchOneByRcpSeq(String rcpSeq);

}
