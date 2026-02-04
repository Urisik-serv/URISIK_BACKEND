package com.urisik.backend.domain.recipe.dto.req;

import lombok.Getter;

import java.util.List;

@Getter
public class TransformRecipeRequestDTO {

    /**
     * Recipe 상세 조회에서 내려온
     * allergyWarning.allergens 값을 그대로 전달
     * 예: ["달걀","새우"]
     */
    private List<String> allergens;

    /**
     * 공개 여부 (선택)
     * PUBLIC / PRIVATE
     * 없으면 PUBLIC
     */
    private String visibility;
}

