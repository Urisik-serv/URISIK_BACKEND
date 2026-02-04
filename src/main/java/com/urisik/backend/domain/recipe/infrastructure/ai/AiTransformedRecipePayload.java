package com.urisik.backend.domain.recipe.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiTransformedRecipePayload {

    private String title;
    private List<String> ingredients;
    private List<Step> steps;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        private Integer order;
        private String description;
    }
}
