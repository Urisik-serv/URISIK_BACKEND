package com.urisik.backend.domain.allergy.service;

import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.AllergenAlternativeRepository;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import com.urisik.backend.global.util.IngredientNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllergySubstitutionService {

    private final MemberAllergyRepository memberAllergyRepository;
    private final AllergenAlternativeRepository allergenAlternativeRepository;
    private final IngredientNormalizer ingredientNormalizer;

    /**
     * 가족 기준 알레르기 판별 후
     * AI에 전달할 "대체 규칙"을 생성한다.
     * 이 서비스는 레시피를 생성하지 않는다.
     */
    public Map<Allergen, List<AllergenAlternative>> generateSubstitutionRules(
            Long familyRoomId,
            List<String> recipeIngredients
    ) {
        // 1. 가족 전체 알레르기 조회
        List<Allergen> familyAllergens =
                memberAllergyRepository.findByFamilyRoomId(familyRoomId)
                        .stream()
                        .map(MemberAllergy::getAllergen)
                        .distinct()
                        .toList();

        if (familyAllergens.isEmpty()) {
            return Map.of();
        }

        // 2. 재료 정규화
        List<String> normalizedIngredients =
                recipeIngredients.stream()
                        .map(ingredientNormalizer::normalize)
                        .toList();

        // 3. 실제 레시피에 포함된 알레르기 필터링
        List<Allergen> matchedAllergens =
                familyAllergens.stream()
                        .filter(allergen ->
                                normalizedIngredients.stream()
                                        .anyMatch(ing ->
                                                ing.contains(allergen.getKoreanName())
                                        )
                        )
                        .toList();

        if (matchedAllergens.isEmpty()) {
            return Map.of();
        }

        // 4. 알레르기 → 대체 식재료 매핑
        List<AllergenAlternative> alternatives =
                allergenAlternativeRepository.findByAllergenIn(matchedAllergens);

        return alternatives.stream()
                .collect(Collectors.groupingBy(
                        AllergenAlternative::getAllergen
                ));
    }
}



