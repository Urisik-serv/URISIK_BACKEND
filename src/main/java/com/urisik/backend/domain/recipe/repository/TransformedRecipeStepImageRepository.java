package com.urisik.backend.domain.recipe.repository;

import com.urisik.backend.domain.recipe.entity.TransformedRecipeStepImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransformedRecipeStepImageRepository
        extends JpaRepository<TransformedRecipeStepImage, Long> {
}