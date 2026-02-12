package com.urisik.backend.domain.recipe.infrastructure.external.foodsafety;

import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.dto.FoodSafetyRecipeResponse;

import java.util.List;

public interface FoodSafetyRecipeClient {

    FoodSafetyRecipeResponse.Row fetchOneByRcpSeq(String rcpSeq);

    List<FoodSafetyRecipeResponse.Row> searchByName(String keyword, int startIdx, int endIdx);

}
