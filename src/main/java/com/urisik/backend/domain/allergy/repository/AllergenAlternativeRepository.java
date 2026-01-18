package com.urisik.backend.domain.allergy.repository;

import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.enums.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllergenAlternativeRepository extends JpaRepository<AllergenAlternative, Long> {

    List<AllergenAlternative> findByAllergenIn(List<Allergen> allergens);

}