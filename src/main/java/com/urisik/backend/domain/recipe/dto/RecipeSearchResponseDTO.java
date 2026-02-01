package com.urisik.backend.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecipeSearchResponseDTO {

    private List<Item> items;

    @Getter
    @AllArgsConstructor
    public static class Item {
        private String id;          // EXTERNAL: "EXT-<RCP_SEQ>", RECIPE: "<recipeId>", TRANSFORMED: "<transformedRecipeId>"
        private String type;        // EXTERNAL / RECIPE / TRANSFORMED
        private String title;
        private String category;    // 요리종류(가능한 경우)
        private String imageUrl;    // 음식 사진(대표)
        private Double avgScore;
        private Integer reviewCount;
    }

}
