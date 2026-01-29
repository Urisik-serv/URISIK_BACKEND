package com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodSafetyRecipeResponse {

    @JsonProperty("COOKRCP01")
    private CookRcp01 cookrcp01;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CookRcp01 {
        @JsonProperty("row")
        private List<Row> row;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Row {
        @JsonProperty("RCP_SEQ")
        private String rcpSeq;

        @JsonProperty("RCP_NM")
        private String rcpNm;

        @JsonProperty("RCP_PAT2")
        private String category; // 요리종류

        @JsonProperty("INFO_WGT")
        private String servingWeight;

        @JsonProperty("INFO_ENG")
        private String calorie;

        @JsonProperty("INFO_CAR")
        private String carbohydrate;

        @JsonProperty("INFO_PRO")
        private String protein;

        @JsonProperty("INFO_FAT")
        private String fat;

        @JsonProperty("INFO_NA")
        private String sodium;

        @JsonProperty("ATT_FILE_NO_MAIN")
        private String imageLarge;

        @JsonProperty("ATT_FILE_NO_MK")
        private String imageSmall;

        @JsonProperty("RCP_PARTS_DTLS")
        private String ingredientsRaw;

        @JsonProperty("MANUAL01")
        private String manual01;
        @JsonProperty("MANUAL02")
        private String manual02;
        @JsonProperty("MANUAL03")
        private String manual03;
        @JsonProperty("MANUAL04")
        private String manual04;
        @JsonProperty("MANUAL05")
        private String manual05;
        @JsonProperty("MANUAL06")
        private String manual06;
        @JsonProperty("MANUAL07")
        private String manual07;
        @JsonProperty("MANUAL08")
        private String manual08;
        @JsonProperty("MANUAL09")
        private String manual09;
        @JsonProperty("MANUAL10")
        private String manual10;
        @JsonProperty("MANUAL11")
        private String manual11;
        @JsonProperty("MANUAL12")
        private String manual12;
        @JsonProperty("MANUAL13")
        private String manual13;
        @JsonProperty("MANUAL14")
        private String manual14;
        @JsonProperty("MANUAL15")
        private String manual15;
        @JsonProperty("MANUAL16")
        private String manual16;
        @JsonProperty("MANUAL17")
        private String manual17;
        @JsonProperty("MANUAL18")
        private String manual18;
        @JsonProperty("MANUAL19")
        private String manual19;
        @JsonProperty("MANUAL20")
        private String manual20;
    }
}
