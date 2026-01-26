package com.urisik.backend.domain.recipe.converter;

import com.urisik.backend.domain.allergy.converter.AllergySubstitutionConverter;
import com.urisik.backend.domain.allergy.dto.res.AllergySubstitutionResponseDTO;
import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.recipe.dto.RecipeDetailDTO;
import com.urisik.backend.domain.recipe.dto.RecipeSummaryDTO;
import com.urisik.backend.domain.recipe.model.RecipeContent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RecipeConverter {

    public RecipeSummaryDTO toSummary(
            RecipeContent content,
            Map<Allergen, List<AllergenAlternative>> subsMap
    ) {
        List<AllergySubstitutionResponseDTO> subsDto =
                AllergySubstitutionConverter.toDtoList(subsMap);

        return new RecipeSummaryDTO(
                content.getRecipeKey(),
                content.getTitle(),
                content.getImageMain(),
                content.getIngredients(),
                !subsDto.isEmpty(),
                subsDto
        );
    }

    public RecipeDetailDTO toDetail(
            RecipeContent content,
            Map<Allergen, List<AllergenAlternative>> subsMap
    ) {
        List<AllergySubstitutionResponseDTO> subsDto =
                AllergySubstitutionConverter.toDtoList(subsMap);

        return new RecipeDetailDTO(
                content.getRecipeKey(),
                content.getTitle(),
                content.getCookingMethod(),
                content.getCategory(),
                content.getNutrition(),
                content.getHashtags(),
                content.getImageMain(),
                content.getImageLarge(),
                content.getIngredients(),
                content.getSteps(),
                content.getTip(),
                !subsDto.isEmpty(),
                subsDto
        );
    }

}
