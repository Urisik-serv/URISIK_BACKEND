package com.urisik.backend.domain.allergy.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AllergySubstitutionResponseDTO {

    private String allergen;              // 알레르기 이름 (한글)
    private List<String> alternatives;    // 대체 식재료 목록

}
