package com.urisik.backend.domain.recipe.mapper;

import com.urisik.backend.domain.recipe.infrastructure.ExternalRecipeRaw;
import com.urisik.backend.domain.recipe.model.Nutrition;
import com.urisik.backend.domain.recipe.model.RecipeContent;
import com.urisik.backend.domain.recipe.model.RecipeStep;
import com.urisik.backend.domain.recipe.util.IngredientParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExternalRecipeMapper {

    private final IngredientParser ingredientParser;

    // includeLargeImage:
    // - 검색: false (MAIN만)
    // - 상세: true  (MAIN+MK)
    public RecipeContent toContent(ExternalRecipeRaw raw, boolean includeLargeImage) {
        return RecipeContent.builder()
                .recipeKey("EXT-" + raw.getRCP_SEQ())
                .title(raw.getRCP_NM())
                .cookingMethod(raw.getRCP_WAY2())
                .category(raw.getRCP_PAT2())
                .nutrition(toNutrition(raw))
                .hashtags(parseHashtags(raw.getHASH_TAG()))
                .imageMain(raw.getATT_FILE_NO_MAIN())
                .imageLarge(includeLargeImage ? raw.getATT_FILE_NO_MK() : null)
                .ingredients(ingredientParser.parse(raw.getRCP_PARTS_DTLS()))
                .steps(parseSteps(raw))
                .tip(raw.getRCP_NA_TIP())
                .build();
    }

    private Nutrition toNutrition(ExternalRecipeRaw raw) {
        return Nutrition.builder()
                .weight(raw.getINFO_WGT())
                .energy(raw.getINFO_ENG())
                .carbohydrate(raw.getINFO_CAR())
                .protein(raw.getINFO_PRO())
                .fat(raw.getINFO_FAT())
                .sodium(raw.getINFO_NA())
                .build();
    }

    private List<String> parseHashtags(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private List<RecipeStep> parseSteps(ExternalRecipeRaw raw) {
        List<RecipeStep> steps = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            String desc = getField(raw, "MANUAL" + String.format("%02d", i));
            if (desc == null || desc.isBlank()) continue;

            String img = getField(raw, "MANUAL_IMG" + String.format("%02d", i));
            steps.add(new RecipeStep(i, desc, (img == null || img.isBlank()) ? null : img));
        }

        return steps;
    }

    private String getField(ExternalRecipeRaw raw, String fieldName) {
        try {
            Field f = ExternalRecipeRaw.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return (String) f.get(raw);
        } catch (Exception e) {
            return null;
        }
    }

}

