package com.urisik.backend.domain.allergy.converter;

import com.urisik.backend.domain.allergy.dto.res.AllergyResponseDTO;
import com.urisik.backend.domain.allergy.enums.Allergen;

import java.util.List;

public class AllergyConverter {

    public static AllergyResponseDTO toDto(Allergen allergen) {
        return new AllergyResponseDTO(allergen.getKoreanName());
    }

    public static List<AllergyResponseDTO> toDtoList(List<Allergen> allergens) {
        return allergens.stream()
                .map(AllergyConverter::toDto)
                .toList();
    }

}
