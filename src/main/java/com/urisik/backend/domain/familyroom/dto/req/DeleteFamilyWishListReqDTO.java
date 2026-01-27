package com.urisik.backend.domain.familyroom.dto.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DeleteFamilyWishListReqDTO(
        @NotEmpty List<@NotNull Long> recipeIds
) {
}
