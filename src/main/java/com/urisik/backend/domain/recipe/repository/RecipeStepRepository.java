package com.urisik.backend.domain.recipe.repository;

import com.urisik.backend.domain.recipe.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {
    List<RecipeStep> findByRecipe_IdOrderByStepOrder(Long recipeId);
}
