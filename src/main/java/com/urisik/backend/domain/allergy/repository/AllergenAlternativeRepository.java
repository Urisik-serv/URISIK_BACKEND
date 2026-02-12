package com.urisik.backend.domain.allergy.repository;

import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.entity.AlternativeIngredient;
import com.urisik.backend.domain.allergy.enums.Allergen;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllergenAlternativeRepository extends JpaRepository<AllergenAlternative, Long> {

    @EntityGraph(attributePaths = "ingredient")
    List<AllergenAlternative> findByAllergenIn(List<Allergen> allergens);

    boolean existsByAllergenAndIngredient(Allergen allergen, AlternativeIngredient ingredient);

    List<AllergenAlternative> findByAllergen(Allergen allergen);

}

