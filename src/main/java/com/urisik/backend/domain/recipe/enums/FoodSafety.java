package com.urisik.backend.domain.recipe.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FoodSafety {

        SAFETY("안전"),
        DANGEROUS("위험");

        private final String displayName;
    }

