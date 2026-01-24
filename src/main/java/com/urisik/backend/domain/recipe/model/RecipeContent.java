package com.urisik.backend.domain.recipe.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecipeContent {

    private String recipeKey;     // EXT-123 / DB-1
    private String title;         // RCP_NM
    private String cookingMethod; // RCP_WAY2
    private String category;      // RCP_PAT2

    private Nutrition nutrition;
    private List<String> hashtags;

    private String imageMain;     // ATT_FILE_NO_MAIN (검색/상세 공통)
    private String imageLarge;    // ATT_FILE_NO_MK   (상세 전용)

    private List<String> ingredients; // 파싱된 재료
    private List<RecipeStep> steps;   // 단계

    private String tip;           // RCP_NA_TIP

}
