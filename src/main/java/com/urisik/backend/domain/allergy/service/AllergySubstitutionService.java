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
     * 사용자 알레르기 + 레시피 재료 → 대체 식재료 매핑
     */
    public Map<Allergen, List<String>> checkAndMapSubstitutions(
            Long memberId,
            List<String> recipeIngredients
    ) {
        //사용자 알레르기 조회
        List<Allergen> userAllergens =
                memberAllergyRepository.findByMemberId(memberId)
                        .stream()
                        .map(MemberAllergy::getAllergen)
                        .toList();

        if (userAllergens.isEmpty()) {
            return Map.of();
        }

        //재료 정규화
        List<String> normalizedIngredients =
                recipeIngredients.stream()
                        .map(ingredientNormalizer::normalize)
                        .toList();

        //실제 레시피에 포함된 알레르기만 추출
        List<Allergen> matchedAllergens =
                userAllergens.stream()
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

        //알레르기 → 대체 식재료 조회
        List<AllergenAlternative> alternatives =
                allergenAlternativeRepository.findByAllergenIn(matchedAllergens);

        //Map 변환
        return alternatives.stream()
                .collect(Collectors.groupingBy(
                        AllergenAlternative::getAllergen,
                        Collectors.mapping(
                                aa -> aa.getIngredient().getName(),
                                Collectors.toList()
                        )
                ));
    }

}
