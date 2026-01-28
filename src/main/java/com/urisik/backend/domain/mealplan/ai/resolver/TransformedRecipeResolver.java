package com.urisik.backend.domain.mealplan.ai.resolver;

public interface TransformedRecipeResolver {

    /**
     * 가족 기준으로 안전하게 변형된 레시피(=transformed_recipe)를 확보하고 id를 반환
     * - 없으면 생성(또는 변형 생성 요청) 후 반환
     */
    Long resolveOrCreate(Long familyRoomId, Long recipeId);
}
