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
        private String category;

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

        /* =========================
           조리 단계 텍스트
           ========================= */

        @JsonProperty("MANUAL01") private String manual01;
        @JsonProperty("MANUAL02") private String manual02;
        @JsonProperty("MANUAL03") private String manual03;
        @JsonProperty("MANUAL04") private String manual04;
        @JsonProperty("MANUAL05") private String manual05;
        @JsonProperty("MANUAL06") private String manual06;
        @JsonProperty("MANUAL07") private String manual07;
        @JsonProperty("MANUAL08") private String manual08;
        @JsonProperty("MANUAL09") private String manual09;
        @JsonProperty("MANUAL10") private String manual10;
        @JsonProperty("MANUAL11") private String manual11;
        @JsonProperty("MANUAL12") private String manual12;
        @JsonProperty("MANUAL13") private String manual13;
        @JsonProperty("MANUAL14") private String manual14;
        @JsonProperty("MANUAL15") private String manual15;
        @JsonProperty("MANUAL16") private String manual16;
        @JsonProperty("MANUAL17") private String manual17;
        @JsonProperty("MANUAL18") private String manual18;
        @JsonProperty("MANUAL19") private String manual19;
        @JsonProperty("MANUAL20") private String manual20;

        /* =========================
           조리 단계 이미지
           ========================= */

        @JsonProperty("MANUAL_IMG01") private String manualImg01;
        @JsonProperty("MANUAL_IMG02") private String manualImg02;
        @JsonProperty("MANUAL_IMG03") private String manualImg03;
        @JsonProperty("MANUAL_IMG04") private String manualImg04;
        @JsonProperty("MANUAL_IMG05") private String manualImg05;
        @JsonProperty("MANUAL_IMG06") private String manualImg06;
        @JsonProperty("MANUAL_IMG07") private String manualImg07;
        @JsonProperty("MANUAL_IMG08") private String manualImg08;
        @JsonProperty("MANUAL_IMG09") private String manualImg09;
        @JsonProperty("MANUAL_IMG10") private String manualImg10;
        @JsonProperty("MANUAL_IMG11") private String manualImg11;
        @JsonProperty("MANUAL_IMG12") private String manualImg12;
        @JsonProperty("MANUAL_IMG13") private String manualImg13;
        @JsonProperty("MANUAL_IMG14") private String manualImg14;
        @JsonProperty("MANUAL_IMG15") private String manualImg15;
        @JsonProperty("MANUAL_IMG16") private String manualImg16;
        @JsonProperty("MANUAL_IMG17") private String manualImg17;
        @JsonProperty("MANUAL_IMG18") private String manualImg18;
        @JsonProperty("MANUAL_IMG19") private String manualImg19;
        @JsonProperty("MANUAL_IMG20") private String manualImg20;

        /* =========================
           단계 접근 헬퍼
           ========================= */

        public String getManual(int i) {
            return switch (i) {
                case 1 -> manual01;
                case 2 -> manual02;
                case 3 -> manual03;
                case 4 -> manual04;
                case 5 -> manual05;
                case 6 -> manual06;
                case 7 -> manual07;
                case 8 -> manual08;
                case 9 -> manual09;
                case 10 -> manual10;
                case 11 -> manual11;
                case 12 -> manual12;
                case 13 -> manual13;
                case 14 -> manual14;
                case 15 -> manual15;
                case 16 -> manual16;
                case 17 -> manual17;
                case 18 -> manual18;
                case 19 -> manual19;
                case 20 -> manual20;
                default -> null;
            };
        }

        public String getManualImg(int i) {
            return switch (i) {
                case 1 -> manualImg01;
                case 2 -> manualImg02;
                case 3 -> manualImg03;
                case 4 -> manualImg04;
                case 5 -> manualImg05;
                case 6 -> manualImg06;
                case 7 -> manualImg07;
                case 8 -> manualImg08;
                case 9 -> manualImg09;
                case 10 -> manualImg10;
                case 11 -> manualImg11;
                case 12 -> manualImg12;
                case 13 -> manualImg13;
                case 14 -> manualImg14;
                case 15 -> manualImg15;
                case 16 -> manualImg16;
                case 17 -> manualImg17;
                case 18 -> manualImg18;
                case 19 -> manualImg19;
                case 20 -> manualImg20;
                default -> null;
            };
        }
    }
}
