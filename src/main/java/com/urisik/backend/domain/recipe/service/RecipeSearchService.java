package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.recipe.converter.RecipeSearchConverter;
import com.urisik.backend.domain.recipe.dto.res.RecipeSearchResponseDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.enums.Visibility;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.FoodSafetyRecipeClient;
import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.dto.FoodSafetyRecipeResponse;
import com.urisik.backend.domain.recipe.repository.RecipeExternalMetadataRepository;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RecipeSearchService {

    private final RecipeRepository recipeRepository;
    private final TransformedRecipeRepository transformedRecipeRepository;
    private final RecipeExternalMetadataRepository metadataRepository;
    private final FoodSafetyRecipeClient foodSafetyRecipeClient;

    private static final Map<String, Integer> TYPE_PRIORITY = Map.of(
            "TRANSFORMED", 0,
            "RECIPE", 1,
            "EXTERNAL", 2
    );

    @Transactional(readOnly = true)
    public RecipeSearchResponseDTO search(String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        List<RecipeSearchResponseDTO.Item> items = new ArrayList<>();

        // 1) 내부 원본 레시피
        List<Recipe> recipes = recipeRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        for (Recipe r : recipes) {
            RecipeExternalMetadata meta = metadataRepository.findByRecipe_Id(r.getId()).orElse(null);
            items.add(RecipeSearchConverter.fromRecipe(r, meta));
        }

        // 2) 공개 변형 레시피
        List<TransformedRecipe> trs =
                transformedRecipeRepository.findPublicByRecipeTitleLike(keyword, Visibility.PUBLIC, pageable);

        for (TransformedRecipe tr : trs) {
            RecipeExternalMetadata meta = metadataRepository.findByRecipe_Id(tr.getBaseRecipe().getId()).orElse(null);
            items.add(RecipeSearchConverter.fromTransformed(tr, meta));
        }

        // 3) 외부 API 검색 ( row를 상세 저장에 쓸 snapshot으로도 내려줌)
        int startIdx = page * size + 1;
        int endIdx = startIdx + size - 1;

        List<FoodSafetyRecipeResponse.Row> externals =
                foodSafetyRecipeClient.searchByName(keyword, startIdx, endIdx);

        for (FoodSafetyRecipeResponse.Row row : externals) {
            String instructionsRaw = joinManuals(row);
            items.add(RecipeSearchConverter.fromExternal(row, instructionsRaw));
        }

        // 4) 리뷰 높은 순 정렬
        items.sort(reviewSortComparator());

        return new RecipeSearchResponseDTO(items);
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

    private String joinManuals(FoodSafetyRecipeResponse.Row row) {
        return Stream.of(
                        row.getManual01(), row.getManual02(), row.getManual03(), row.getManual04(), row.getManual05(),
                        row.getManual06(), row.getManual07(), row.getManual08(), row.getManual09(), row.getManual10(),
                        row.getManual11(), row.getManual12(), row.getManual13(), row.getManual14(), row.getManual15(),
                        row.getManual16(), row.getManual17(), row.getManual18(), row.getManual19(), row.getManual20()
                )
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }
}


