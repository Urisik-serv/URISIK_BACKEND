package com.urisik.backend.domain.recipe.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AllergyWarningDTO {
    private boolean hasRisk;
    private List<String> allergens;
}