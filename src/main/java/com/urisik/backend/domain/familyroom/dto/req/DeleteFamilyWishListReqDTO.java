package com.urisik.backend.domain.familyroom.dto.req;

import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record DeleteFamilyWishListReqDTO(
        List<@NotNull Long> recipeId,
        List<@NotNull Long> transformedRecipeId
) {
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isAnyIdsProvided() {
        boolean hasRecipeIds = recipeId != null && !recipeId.isEmpty();
        boolean hasTransformedIds = transformedRecipeId != null && !transformedRecipeId.isEmpty();
        return hasRecipeIds || hasTransformedIds;
    }
}
