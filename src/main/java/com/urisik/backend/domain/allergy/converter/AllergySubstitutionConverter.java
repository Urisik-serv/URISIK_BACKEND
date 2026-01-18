package com.urisik.backend.domain.allergy.converter;

import com.urisik.backend.domain.allergy.dto.res.AllergySubstitutionResponseDTO;
import com.urisik.backend.domain.allergy.enums.Allergen;

import java.util.List;
import java.util.Map;

public class AllergySubstitutionConverter {

    private AllergySubstitutionConverter() {}

    public static List<AllergySubstitutionResponseDTO> toDtoList(
            Map<Allergen, List<String>> source
    ) {
        return source.entrySet().stream()
                .map(e -> new AllergySubstitutionResponseDTO(
                        e.getKey().getKoreanName(),
                        e.getValue()
                ))
                .toList();
    }

}
