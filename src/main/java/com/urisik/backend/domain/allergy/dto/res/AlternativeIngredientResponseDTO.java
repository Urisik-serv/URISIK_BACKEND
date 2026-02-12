package com.urisik.backend.domain.allergy.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlternativeIngredientResponseDTO {

    private String name;    // 대체 식재료 이름
    private String reason;  // 대체 이유

}
