package com.urisik.backend.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubstitutionReasonDTO {
    private String allergen;
    private String replacedWith;
    private String reason;
}