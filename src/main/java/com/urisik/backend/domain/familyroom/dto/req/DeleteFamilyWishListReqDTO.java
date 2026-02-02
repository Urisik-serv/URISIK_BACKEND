package com.urisik.backend.domain.familyroom.dto.req;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record DeleteFamilyWishListReqDTO(
        List<@NotNull Long> recipeId,
        List<@NotNull Long> transformedRecipeId
) {
    @AssertTrue(message = "recipeId 또는 transformedRecipeId 중 하나는 반드시 존재해야 합니다.")
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isAnyIdsProvided() {
        boolean hasRecipeIds = recipeId != null && !recipeId.isEmpty();
        boolean hasTransformedIds = transformedRecipeId != null && !transformedRecipeId.isEmpty();
        return hasRecipeIds || hasTransformedIds;
    }
}
