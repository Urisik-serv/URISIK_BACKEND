package com.urisik.backend.domain.familyroom.dto.req;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DeleteFamilyWishListReqDTO(
        @NotEmpty List<Long> recipeIds
) {
}
