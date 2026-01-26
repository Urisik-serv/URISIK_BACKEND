package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.service.AllergySubstitutionService;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.converter.RecipeConverter;
import com.urisik.backend.domain.recipe.dto.RecipeSearchResponseDTO;
import com.urisik.backend.domain.recipe.dto.RecipeSummaryDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.infrastructure.ExternalRecipeRaw;
import com.urisik.backend.domain.recipe.infrastructure.FoodSafetyRecipeClient;
import com.urisik.backend.domain.recipe.mapper.ExternalRecipeMapper;
import com.urisik.backend.domain.recipe.model.RecipeContent;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecipeSearchService {

    private final RecipeRepository recipeRepository;
    private final FoodSafetyRecipeClient foodSafetyRecipeClient;
    private final ExternalRecipeMapper externalRecipeMapper;

    private final AllergySubstitutionService allergySubstitutionService;
    private final RecipeConverter recipeConverter;

    public RecipeSearchResponseDTO search(String name, Long memberId, int limit) {
        if (name == null || name.isBlank()) {
            throw new GeneralException(
                    RecipeErrorCode.RECIPE_NOT_FOUND,
                    "검색어가 비어있습니다."
            );
        }

        // 1) 내부 DB (Case 1)
        List<Recipe> dbRecipes = recipeRepository.findByNameContaining(name);
        if (!dbRecipes.isEmpty()) {
            List<RecipeSummaryDTO> out = new ArrayList<>();

            for (Recipe r : dbRecipes) {
                RecipeContent content = RecipeContent.builder()
                        .recipeKey("DB-" + r.getId())
                        .title(r.getName())
                        .ingredients(r.getIngredients())
                        .build();

                // 타입 변경
                Map<Allergen, List<AllergenAlternative>> subs =
                        allergySubstitutionService.checkAndMapSubstitutions(
                                memberId,
                                content.getIngredients()
                        );

                out.add(recipeConverter.toSummary(content, subs));
            }

            return new RecipeSearchResponseDTO("INTERNAL_DB", out);
        }

        // 2) 외부 API (Case 2)
        List<ExternalRecipeRaw> raws =
                foodSafetyRecipeClient.searchByName(name, 1, Math.min(limit, 50));

        if (raws.isEmpty()) {
            throw new GeneralException(RecipeErrorCode.RECIPE_NOT_FOUND);
        }

        List<RecipeSummaryDTO> out = new ArrayList<>();
        for (ExternalRecipeRaw raw : raws) {
            RecipeContent content = externalRecipeMapper.toContent(raw, false);

            // 타입 변경
            Map<Allergen, List<AllergenAlternative>> subs =
                    allergySubstitutionService.checkAndMapSubstitutions(
                            memberId,
                            content.getIngredients()
                    );

            out.add(recipeConverter.toSummary(content, subs));
        }

        return new RecipeSearchResponseDTO("EXTERNAL_API", out);
    }
}


