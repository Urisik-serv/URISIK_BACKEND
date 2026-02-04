package com.urisik.backend.domain.recipe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.AllergenAlternativeRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.recipe.converter.TransformedRecipeConverter;
import com.urisik.backend.domain.recipe.dto.req.TransformRecipeRequestDTO;
import com.urisik.backend.domain.recipe.dto.res.TransformedRecipeResponseDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.enums.Visibility;
import com.urisik.backend.domain.recipe.infrastructure.ai.AiTransformedRecipePayload;
import com.urisik.backend.domain.recipe.infrastructure.ai.RecipeStepNormalizer;
import com.urisik.backend.domain.recipe.infrastructure.ai.RecipeStepSerializer;
import com.urisik.backend.domain.recipe.infrastructure.ai.RecipeTransformPromptBuilder;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import com.urisik.backend.global.ai.AiClient;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipeTransformService {

    private final RecipeRepository recipeRepository;
    private final TransformedRecipeRepository transformedRecipeRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final AllergenAlternativeRepository allergenAlternativeRepository;

    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public TransformedRecipeResponseDTO transform(
            Long recipeId,
            Long loginUserId,
            TransformRecipeRequestDTO request
    ) {

        /* 1. 원본 레시피 */
        Recipe baseRecipe = recipeRepository.findById(recipeId)
                .orElseThrow(() ->
                        new GeneralException(RecipeErrorCode.RECIPE_NOT_FOUND));

        /* 2. allergens 검증 */
        if (request == null ||
                request.getAllergens() == null ||
                request.getAllergens().isEmpty()) {

            throw new GeneralException(
                    RecipeErrorCode.RECIPE_NO_ALLERGY_RISK
            );
        }

        /* 3. familyRoomId */
        FamilyMemberProfile profile =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() ->
                                new GeneralException(GeneralErrorCode.NOT_FOUND));

        Long familyRoomId = profile.getFamilyRoom().getId();

        /* 4. 한글 → Allergen Enum */
        List<Allergen> allergens = request.getAllergens().stream()
                .map(Allergen::fromKorean)
                .distinct()
                .toList();

        /* 5. 대체 식재료 매핑 */
        List<AllergenAlternative> alternatives =
                allergenAlternativeRepository.findByAllergenIn(allergens);

        Map<Allergen, List<AllergenAlternative>> grouped =
                alternatives.stream()
                        .collect(Collectors.groupingBy(
                                AllergenAlternative::getAllergen
                        ));

        for (Allergen allergen : allergens) {
            if (!grouped.containsKey(allergen)) {
                throw new GeneralException(
                        RecipeErrorCode.ALLERGY_REPLACEMENT_NOT_FOUND
                );
            }
        }

        /* 6. 대표 대체 재료 */
        Map<Allergen, AllergenAlternative> primary = new LinkedHashMap<>();
        for (Allergen allergen : allergens) {
            primary.put(allergen, grouped.get(allergen).get(0));
        }

        /* 7. Prompt 생성 */
        String prompt = RecipeTransformPromptBuilder.build(
                baseRecipe,
                primary
        );

        /* 8. AI 호출 */
        String json = aiClient.generateJson(prompt);

        AiTransformedRecipePayload payload;
        try {
            payload = objectMapper.readValue(
                    json,
                    AiTransformedRecipePayload.class
            );
        } catch (Exception e) {
            throw new GeneralException(
                    RecipeErrorCode.AI_GENERATION_FAILED
            );
        }

        /* 9. 저장 */
        Visibility visibility = parseVisibility(request.getVisibility());

        List<TransformedRecipeResponseDTO.SubstitutionSummaryDTO> summary =
                primary.values().stream()
                        .map(alt -> new TransformedRecipeResponseDTO.SubstitutionSummaryDTO(
                                alt.getAllergen(),
                                alt.getIngredient().getName(),
                                alt.getReason()
                        ))
                        .toList();

        TransformedRecipe saved =
                transformedRecipeRepository.save(
                        new TransformedRecipe(
                                baseRecipe,
                                familyRoomId,
                                payload.getTitle(),
                                visibility,
                                String.join("\n", payload.getIngredients()),
                                RecipeStepSerializer.serialize(payload.getSteps()),
                                toJson(summary)
                        )
                );

        return TransformedRecipeConverter.toDto(
                saved,
                payload.getIngredients(),
                RecipeStepNormalizer.normalize(payload.getSteps()),
                summary
        );
    }

    private Visibility parseVisibility(String v) {
        if (v == null || v.isBlank()) return Visibility.PUBLIC;
        try {
            return Visibility.valueOf(v.trim().toUpperCase());
        } catch (Exception e) {
            return Visibility.PUBLIC;
        }
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "[]";
        }
    }
}


