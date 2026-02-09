package com.urisik.backend.domain.recommendation.candidate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urisik.backend.domain.recommendation.policy.CategoryMapper;
import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.recipe.dto.res.SubstitutionReasonDTO;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TransformedRecipeCandidateLow implements HighScoreRecipeCandidate{

    private final TransformedRecipe tr;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override public Long getId() { return tr.getId(); }
    @Override public String getTitle() { return tr.getTitle(); }

    @Override
    public String getImageUrl() {
        return tr.getBaseRecipe().getRecipeExternalMetadata() != null
                ? tr.getBaseRecipe().getRecipeExternalMetadata().getImageLargeUrl()
                : null;
    }

    @Override
    public String getCategory() {
        return tr.getBaseRecipe().getRecipeExternalMetadata() != null
                ? CategoryMapper.map(
                tr.getBaseRecipe()
                        .getRecipeExternalMetadata()
                        .getCategory()
        )
                : CategoryMapper.map(null);
    }


    @Override public double getAvgScore() {
        return tr.getBaseRecipe().getAvgScore();
    }

    @Override
    public List<String> getIngredients() {
        return RecipeTextParser.parseIngredients(
                tr.getIngredientsRaw()
        );
    }

    @Override
    public String getDescription() {
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
            // 실패 시 fallback
            return null;
        }
    }

    @Override public int getReviewCount() {
        return tr.getBaseRecipe().getReviewCount();
    }

    @Override public int getWishCount() {
        return tr.getWishCount();
    }

}
