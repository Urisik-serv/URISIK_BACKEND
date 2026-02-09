package com.urisik.backend.domain.familyroom.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DeleteFamilyWishListReqDTO(
        @NotEmpty @Valid
        @Schema(description = "삭제할 항목 목록")
        List<ItemKey> items
) {
    public record ItemKey(
            @NotBlank
            @Schema(description = "항목 타입", allowableValues = {"RECIPE", "TRANSFORMED_RECIPE"})
            String type,

            @NotNull
            @Schema(description = "항목 ID")
            Long id
    ) {}
}
