package com.urisik.backend.domain.allergy.repository;

import com.urisik.backend.domain.allergy.entity.AlternativeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlternativeIngredientRepository extends JpaRepository<AlternativeIngredient, Long> {

    Optional<AlternativeIngredient> findByName(String name);

}
