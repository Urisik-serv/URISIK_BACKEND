package com.urisik.backend.domain.recipe.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExternalRecipeUpsertResponseDTO {
    private Long recipeId;
    private boolean created; // 새로 저장됐는지 여부
}

