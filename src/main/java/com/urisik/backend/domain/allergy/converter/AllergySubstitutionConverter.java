package com.urisik.backend.domain.allergy.converter;

import com.urisik.backend.domain.allergy.dto.res.AllergySubstitutionResponseDTO;
import com.urisik.backend.domain.allergy.dto.res.AlternativeIngredientResponseDTO;
import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.enums.Allergen;

import java.util.List;
import java.util.Map;

public class AllergySubstitutionConverter {

    private AllergySubstitutionConverter() {}

    public static List<AllergySubstitutionResponseDTO> toDtoList(
            Map<Allergen, List<AllergenAlternative>> source
    ) {
        return source.entrySet().stream()
                .map(entry -> new AllergySubstitutionResponseDTO(
                        entry.getKey().getKoreanName(),
                        entry.getValue().stream()
                                .map(alt -> new AlternativeIngredientResponseDTO(
                                        alt.getIngredient().getName(),
                                        alt.getReason()
                                ))
                                .toList()
                ))
                .toList();
    }

}



