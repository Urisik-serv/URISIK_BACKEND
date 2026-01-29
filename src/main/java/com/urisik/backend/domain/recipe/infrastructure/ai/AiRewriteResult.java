package com.urisik.backend.domain.recipe.infrastructure.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiRewriteResult {
    private String ingredientsTransformedRaw;
    private String instructionsTransformedRaw;
}
