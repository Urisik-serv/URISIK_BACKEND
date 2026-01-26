package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.service.AllergySubstitutionService;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.converter.RecipeConverter;
import com.urisik.backend.domain.recipe.dto.RecipeDetailDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.infrastructure.ExternalRecipeRaw;
import com.urisik.backend.domain.recipe.infrastructure.FoodSafetyRecipeClient;
import com.urisik.backend.domain.recipe.mapper.ExternalRecipeMapper;
import com.urisik.backend.domain.recipe.model.RecipeContent;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecipeDetailService {

    private final RecipeRepository recipeRepository;
    private final FoodSafetyRecipeClient foodSafetyRecipeClient;
    private final ExternalRecipeMapper externalRecipeMapper;

    private final AllergySubstitutionService allergySubstitutionService;
    private final RecipeConverter recipeConverter;

    public RecipeDetailDTO getDetail(String recipeKey, Long memberId) {
        RecipeKeyParser.Parsed parsed = RecipeKeyParser.parse(recipeKey);

        // 1) DB
        if (parsed.source().equals("DB")) {
            Long id;
            try {
                id = Long.parseLong(parsed.id());
            } catch (NumberFormatException e) {
                throw new GeneralException(RecipeErrorCode.RECIPE_INVALID_KEY);
            }

            Recipe r = recipeRepository.findById(id)
                    .orElseThrow(() -> new GeneralException(RecipeErrorCode.RECIPE_NOT_FOUND));

            RecipeContent content = RecipeContent.builder()
                    .recipeKey("DB-" + r.getId())
                    .title(r.getName())
                    .ingredients(r.getIngredients())
                    .steps(List.of())
                    .hashtags(List.of())
                    .build();

            //타입 변경
            Map<Allergen, List<AllergenAlternative>> subs =
                    allergySubstitutionService.checkAndMapSubstitutions(
                            memberId,
                            content.getIngredients()
                    );

            return recipeConverter.toDetail(content, subs);
        }

        // 2) EXTERNAL
        if (parsed.source().equals("EXT")) {
            ExternalRecipeRaw raw =
                    foodSafetyRecipeClient.findByRcpSeq(parsed.id());

            if (raw == null) {
                throw new GeneralException(RecipeErrorCode.RECIPE_NOT_FOUND);
            }

            RecipeContent content = externalRecipeMapper.toContent(raw, true);

            //타입 변경
            Map<Allergen, List<AllergenAlternative>> subs =
                    allergySubstitutionService.checkAndMapSubstitutions(
                            memberId,
                            content.getIngredients()
                    );

            return recipeConverter.toDetail(content, subs);
        }

        throw new GeneralException(RecipeErrorCode.RECIPE_INVALID_KEY);
    }
}

