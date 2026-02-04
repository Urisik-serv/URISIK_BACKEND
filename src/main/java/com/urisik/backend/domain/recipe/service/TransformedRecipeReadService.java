package com.urisik.backend.domain.recipe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.recipe.dto.res.TransformedRecipeDetailResponseDTO;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransformedRecipeReadService {

    private final TransformedRecipeRepository transformedRecipeRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final AllergyRiskService allergyRiskService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransformedRecipeDetailResponseDTO getTransformedRecipeDetail(
            Long transformedRecipeId,
            Long loginUserId
    ) {
        TransformedRecipe tr = transformedRecipeRepository.findById(transformedRecipeId)
                .orElseThrow(() -> new GeneralException(RecipeErrorCode.TRANSFORMED_RECIPE_NOT_FOUND));

        FamilyMemberProfile profile =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));

        Long myFamilyRoomId = profile.getFamilyRoom().getId();

        List<String> ingredients = RecipeTextParser.parseIngredients(tr.getIngredientsRaw());
        List<Allergen> risky = allergyRiskService.detectRiskAllergens(myFamilyRoomId, ingredients);

        boolean createdByFamily = tr.getFamilyRoomId().equals(myFamilyRoomId);

        List<TransformedRecipeDetailResponseDTO.SubstitutionSummaryDTO> subs =
                parseSubstitutionSummary(tr.getSubstitutionSummaryJson());

        TransformedRecipeDetailResponseDTO.WarningDTO warning =
                risky.isEmpty()
                        ? new TransformedRecipeDetailResponseDTO.WarningDTO(false, "우리 가족 기준으로 안전합니다.", List.of())
                        : new TransformedRecipeDetailResponseDTO.WarningDTO(true, "우리 가족 기준으로 알레르기 위험이 있습니다.", risky.stream().map(Allergen::getKoreanName).toList());

        return new TransformedRecipeDetailResponseDTO(
                tr.getId(),
                tr.getBaseRecipe().getTitle(), // 원하면 변형 title 필드 따로 두고 관리 가능
                tr.getBaseRecipe().getId(),
                ingredients,
                RecipeTextParser.parseSteps(tr.getInstructionsRaw()),
                subs,
                warning,
                createdByFamily
        );
    }

    private List<TransformedRecipeDetailResponseDTO.SubstitutionSummaryDTO> parseSubstitutionSummary(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}

