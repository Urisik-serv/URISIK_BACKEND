package com.urisik.backend.domain.recipe.repository;

import com.urisik.backend.domain.recipe.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Collection;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {
    List<RecipeStep> findAllByRecipe_IdInOrderByRecipe_IdAscStepOrderAsc(Collection<Long> recipeIds);

    List<RecipeStep>
    findByRecipe_IdOrderByStepOrderAsc(Long recipeId);
}
