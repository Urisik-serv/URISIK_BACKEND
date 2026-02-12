package com.urisik.backend.domain.recipe.repository;

import com.urisik.backend.domain.recipe.entity.TransformedRecipeStepImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransformedRecipeStepImageRepository
        extends JpaRepository<TransformedRecipeStepImage, Long> {

    List<TransformedRecipeStepImage> findAllByTransformedRecipeIdInOrderByTransformedRecipeIdAscStepOrderAsc(
            List<Long> transformedRecipeIds
    );

    List<TransformedRecipeStepImage>
    findByTransformedRecipeIdOrderByStepOrderAsc(Long transformedRecipeId);
}