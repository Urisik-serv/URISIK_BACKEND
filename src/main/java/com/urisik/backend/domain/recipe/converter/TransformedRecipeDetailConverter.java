package com.urisik.backend.domain.recipe.converter;

import com.urisik.backend.domain.recipe.dto.RecipeStepDTO;
import com.urisik.backend.domain.recipe.dto.RecipeStepDetailDTO;
import com.urisik.backend.domain.recipe.entity.RecipeStep;
import com.urisik.backend.domain.recipe.entity.TransformedRecipeStepImage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TransformedRecipeDetailConverter {

    public List<RecipeStepDetailDTO> buildSteps(
            List<RecipeStepDTO> parsedSteps,
            List<TransformedRecipeStepImage> aiImages,
            List<RecipeStep> originalSteps
    ) {

        Map<Integer, String> aiImageMap =
                aiImages.stream()
                        .collect(Collectors.toMap(
                                TransformedRecipeStepImage::getStepOrder,
                                TransformedRecipeStepImage::getImageUrl
                        ));

        Map<Integer, String> originalImageMap =
                originalSteps.stream()
                        .collect(Collectors.toMap(
                                RecipeStep::getStepOrder,
                                RecipeStep::getImageUrl
                        ));

        return parsedSteps.stream()
                .map(step -> {

                    String imageUrl =
                            aiImageMap.getOrDefault(
                                    step.getOrder(),
                                    originalImageMap.get(step.getOrder())
                            );

                    return new RecipeStepDetailDTO(
                            step.getOrder(),
                            step.getDescription(),
                            imageUrl
                    );
                })
                .toList();
    }
}

