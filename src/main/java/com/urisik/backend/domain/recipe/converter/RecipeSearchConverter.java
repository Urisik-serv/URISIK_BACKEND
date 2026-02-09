package com.urisik.backend.domain.recipe.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urisik.backend.domain.recipe.dto.RecipeStepDetailDTO;
import com.urisik.backend.domain.recipe.dto.SubstitutionReasonDTO;
import com.urisik.backend.domain.recipe.dto.res.*;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.dto.FoodSafetyRecipeResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class RecipeSearchConverter {

    private RecipeSearchConverter() {}

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** 내부 원본 레시피 */
    public static RecipeSearchResponseDTO.Item fromRecipe(
            Recipe recipe,
            RecipeExternalMetadata meta,
            Boolean safe
    ) {
        return new RecipeSearchResponseDTO.Item(
                recipe.getId().toString(),
                "RECIPE",
                recipe.getTitle(),
                meta != null ? meta.getImageSmallUrl() : null,
                meta != null ? meta.getCategory() : null,
                recipe.getAvgScore(),
                recipe.getReviewCount(),
                recipe.getWishCount(),
                buildRecipeDescription(recipe),
                safe,
                null

        );
    }

    /** 변형 레시피 */
    public static RecipeSearchResponseDTO.Item fromTransformed(
            TransformedRecipe tr,
            RecipeExternalMetadata meta,
            Boolean safe
    ) {
        return new RecipeSearchResponseDTO.Item(
                tr.getId().toString(),
                "TRANSFORMED",
                tr.getTitle(),
                meta != null ? meta.getImageSmallUrl() : null,
                meta != null ? meta.getCategory() : null,
                tr.getAvgScore(),
                tr.getReviewCount(),
                tr.getWishCount(),
                buildTransformedDescription(tr),
                safe,
                null
        );
    }

    /** 외부 API 레시피 */
    public static RecipeSearchResponseDTO.Item fromExternal(FoodSafetyRecipeResponse.Row row) {

        List<RecipeStepDetailDTO> steps = buildSteps(row);

        RecipeSearchResponseDTO.ExternalSnapshot snapshot =
                new RecipeSearchResponseDTO.ExternalSnapshot(
                        row.getRcpSeq(),
                        row.getRcpNm(),
                        row.getCategory(),
                        row.getServingWeight(),
                        row.getCalorie(),
                        row.getCarbohydrate(),
                        row.getProtein(),
                        row.getFat(),
                        row.getSodium(),
                        row.getImageSmall(),
                        row.getImageLarge(),
                        row.getIngredientsRaw(),
                        steps
                );

        return new RecipeSearchResponseDTO.Item(
                "EXT-" + row.getRcpSeq(),
                "EXTERNAL",
                row.getRcpNm(),
                row.getImageSmall(),
                row.getCategory(),
                null,
                null,
                null,
                null,
                null,
                snapshot
        );
    }
    private static List<RecipeStepDetailDTO> buildSteps(FoodSafetyRecipeResponse.Row row) {
        List<RecipeStepDetailDTO> steps = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            String manual = row.getManual(i);
            String img = row.getManualImg(i);

            if (manual != null && !manual.isBlank()) {
                steps.add(new RecipeStepDetailDTO(
                        i,
                        manual.trim(),
                        (img != null && !img.isBlank()) ? img : null
                ));
            }
        }
        return steps;
    }


    private static String buildRecipeDescription(Recipe recipe) {
        List<String> ingredients =
                RecipeTextParser.parseIngredients(recipe.getIngredientsRaw());

        return ingredients.stream()
                .collect(Collectors.joining(", "));
    }

    private static String buildTransformedDescription(TransformedRecipe tr) {
        String json = tr.getSubstitutionSummaryJson();

        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            List<SubstitutionReasonDTO> list =
                    objectMapper.readValue(
                            json,
                            new TypeReference<List<SubstitutionReasonDTO>>() {}
                    );

            return list.stream()
                    .map(SubstitutionReasonDTO::getReason)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.joining(" · "));
        } catch (Exception e) {
            log.error("Failed to parse substitutionSummaryJson: {}", json, e);
            return null;
        }
    }


}



