package com.urisik.backend.domain.recipe.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalRecipeRaw {

    private String RCP_SEQ;
    private String RCP_NM;
    private String RCP_WAY2;
    private String RCP_PAT2;

    private String INFO_WGT;
    private String INFO_ENG;
    private String INFO_CAR;
    private String INFO_PRO;
    private String INFO_FAT;
    private String INFO_NA;

    private String HASH_TAG;

    private String ATT_FILE_NO_MAIN;
    private String ATT_FILE_NO_MK;

    private String RCP_PARTS_DTLS;

    private String MANUAL01;  private String MANUAL_IMG01;
    private String MANUAL02;  private String MANUAL_IMG02;
    private String MANUAL03;  private String MANUAL_IMG03;
    private String MANUAL04;  private String MANUAL_IMG04;
    private String MANUAL05;  private String MANUAL_IMG05;
    private String MANUAL06;  private String MANUAL_IMG06;
    private String MANUAL07;  private String MANUAL_IMG07;
    private String MANUAL08;  private String MANUAL_IMG08;
    private String MANUAL09;  private String MANUAL_IMG09;
    private String MANUAL10;  private String MANUAL_IMG10;
    private String MANUAL11;  private String MANUAL_IMG11;
    private String MANUAL12;  private String MANUAL_IMG12;
    private String MANUAL13;  private String MANUAL_IMG13;
    private String MANUAL14;  private String MANUAL_IMG14;
    private String MANUAL15;  private String MANUAL_IMG15;
    private String MANUAL16;  private String MANUAL_IMG16;
    private String MANUAL17;  private String MANUAL_IMG17;
    private String MANUAL18;  private String MANUAL_IMG18;
    private String MANUAL19;  private String MANUAL_IMG19;
    private String MANUAL20;  private String MANUAL_IMG20;

    private String RCP_NA_TIP;

}

