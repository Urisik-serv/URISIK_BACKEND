package com.urisik.backend.domain.recipe.repository;

import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.enums.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransformedRecipeRepository extends JpaRepository<TransformedRecipe, Long> {

    Optional<TransformedRecipe> findByRecipe_IdAndFamilyRoomId(Long recipeId, Long familyRoomId);
    List<TransformedRecipe> findByFamilyRoomId(Long familyRoomId);
    List<TransformedRecipe> findByVisibility(Visibility visibility);

}
