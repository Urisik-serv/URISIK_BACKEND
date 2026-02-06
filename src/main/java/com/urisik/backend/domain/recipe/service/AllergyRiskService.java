package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import com.urisik.backend.global.util.IngredientNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AllergyRiskService {

    private final MemberAllergyRepository memberAllergyRepository;
    private final IngredientNormalizer ingredientNormalizer;

    public List<Allergen> detectRiskAllergens(Long familyRoomId, List<String> ingredients) {
        List<Allergen> familyAllergens =
                memberAllergyRepository.findByFamilyRoomId(familyRoomId)
                        .stream()
                        .map(MemberAllergy::getAllergen)
                        .distinct()
                        .toList();

        if (familyAllergens.isEmpty()) return List.of();

        List<String> normalized =
                ingredients.stream().map(ingredientNormalizer::normalize).toList();

        return familyAllergens.stream()
                .filter(a -> normalized.stream().anyMatch(a::matchesIngredient))
                .toList();

    }
}
