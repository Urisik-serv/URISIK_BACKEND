package com.urisik.backend.domain.recipe.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Nutrition {

    private String weight;        // INFO_WGT
    private String energy;        // INFO_ENG
    private String carbohydrate;  // INFO_CAR
    private String protein;       // INFO_PRO
    private String fat;           // INFO_FAT
    private String sodium;        // INFO_NA

}

