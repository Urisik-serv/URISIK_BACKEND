package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.recipe.dto.RecipeStepDTO;
import com.urisik.backend.domain.recipe.entity.TransformedRecipeStepImage;
import com.urisik.backend.domain.recipe.infrastructure.external.ai.GeminiImagePromptBuilder;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeStepImageRepository;
import com.urisik.backend.global.ai.AiImageClient;
import com.urisik.backend.global.external.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransformedRecipeStepImageAsyncService {

    private final AiImageClient aiImageClient;
    private final S3Uploader s3Uploader;
    private final TransformedRecipeStepImageRepository stepImageRepository;

    @Async
    public void generateStepImagesAsync(
            Long transformedRecipeId,
            String recipeTitle,
            List<RecipeStepDTO> steps
    ) {
        for (RecipeStepDTO step : steps) {
            try {
                aiImageClient.generateImage(
                        GeminiImagePromptBuilder.stepImage(recipeTitle, step)
                ).ifPresent(bytes -> {

                    String url = s3Uploader.uploadBytes(
                            bytes,
                            "step_" + step.getOrder() + ".png",
                            "image/png",
                            "transformed-recipe-step"
                    );

                    stepImageRepository.save(
                            new TransformedRecipeStepImage(
                                    transformedRecipeId,
                                    step.getOrder(),
                                    url
                            ));
                });
            } catch (Exception e) {
                log.warn("[ASYNC][STEP IMAGE] failed (step={})", step.getOrder(), e);
            }
        }
    }
}

