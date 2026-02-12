package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.recipe.converter.RecipeSearchConverter;
import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.recipe.dto.res.RecipeSearchResponseDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.FoodSafetyRecipeClient;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.dto.FoodSafetyRecipeResponse;
import com.urisik.backend.domain.recipe.repository.RecipeExternalMetadataRepository;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import com.urisik.backend.domain.search.service.SearchLogService;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecipeSearchService {

    private final RecipeRepository recipeRepository;
    private final TransformedRecipeRepository transformedRecipeRepository;
    private final RecipeExternalMetadataRepository metadataRepository;
    private final FoodSafetyRecipeClient foodSafetyRecipeClient;
    private final AllergyRiskService allergyRiskService;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final SearchLogService searchLogService;

    private static final Map<String, Integer> TYPE_PRIORITY = Map.of(
            "TRANSFORMED", 0,
            "RECIPE", 1,
            "EXTERNAL", 2
    );

    @Transactional(readOnly = true)
    public RecipeSearchResponseDTO search(Long loginUserId,String keyword, int page, int size) {

        searchLogService.logSearch(loginUserId, keyword);

        FamilyMemberProfile profile =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() ->
                                new GeneralException(GeneralErrorCode.NOT_FOUND));

        Long familyRoomId = profile.getFamilyRoom().getId();



        PageRequest pageable = PageRequest.of(page, size);
        List<RecipeSearchResponseDTO.Item> items = new ArrayList<>();

        // 1) 내부 원본 레시피
        List<Recipe> recipes = recipeRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        for (Recipe r : recipes) {
            RecipeExternalMetadata meta = metadataRepository.findByRecipe_Id(r.getId()).orElse(null);

            Boolean safe = determineSafety(
                    familyRoomId,
                    RecipeTextParser.parseIngredients(r.getIngredientsRaw())
            );

            items.add(RecipeSearchConverter.fromRecipe(r, meta, safe));
        }

        // 2) 공개 변형 레시피
        List<TransformedRecipe> trs =
                transformedRecipeRepository.findByTitleLike(keyword, pageable);

        for (TransformedRecipe tr : trs) {
            RecipeExternalMetadata meta = metadataRepository.findByRecipe_Id(tr.getBaseRecipe().getId()).orElse(null);

            Boolean safe = determineSafety(
                    familyRoomId,
                    RecipeTextParser.parseIngredients(
                            tr.getBaseRecipe().getIngredientsRaw()
                    )
            );

            items.add(RecipeSearchConverter.fromTransformed(tr, meta, safe));
        }

        // 3) 외부 API 검색 ( row를 상세 저장에 쓸 snapshot으로도 내려줌)
        int startIdx = page * size + 1;
        int endIdx = startIdx + size - 1;

        List<FoodSafetyRecipeResponse.Row> externals =
                foodSafetyRecipeClient.searchByName(keyword, startIdx, endIdx);

        for (FoodSafetyRecipeResponse.Row row : externals) {
            items.add(RecipeSearchConverter.fromExternal(row));
        }

        // 4) 리뷰 높은 순 정렬
        items.sort(reviewSortComparator());

        return new RecipeSearchResponseDTO(items);
    }

    private Boolean determineSafety(
            Long familyRoomId,
            List<String> ingredients
    ) {
        if (familyRoomId == null) {
            return null;
        }

        return allergyRiskService
                .detectRiskAllergens(familyRoomId, ingredients)
                .isEmpty();
    }

    private Comparator<RecipeSearchResponseDTO.Item> reviewSortComparator() {
        return Comparator
                .comparing(
                        RecipeSearchResponseDTO.Item::getAvgScore,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
                .thenComparing(
                        RecipeSearchResponseDTO.Item::getReviewCount,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
                .thenComparing(item -> TYPE_PRIORITY.getOrDefault(item.getType(), 99))
                .thenComparing(RecipeSearchResponseDTO.Item::getTitle);
    }

}


