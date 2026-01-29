package com.urisik.backend.domain.recipe.repository;

import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipeExternalMetadataRepository extends JpaRepository<RecipeExternalMetadata, Long> {

    Optional<RecipeExternalMetadata> findByRecipe_Id(Long recipeId);

}

