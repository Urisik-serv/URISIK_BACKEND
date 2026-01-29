package com.urisik.backend.domain.allergy.controller;

import com.urisik.backend.domain.allergy.converter.AllergySubstitutionConverter;
import com.urisik.backend.domain.allergy.dto.req.RecipeAllergyCheckRequestDTO;
import com.urisik.backend.domain.allergy.dto.res.AllergySubstitutionResponseDTO;
import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.enums.AllergySuccessCode;
import com.urisik.backend.domain.allergy.service.AllergySubstitutionService;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeAllergyController {

    private final AllergySubstitutionService allergySubstitutionService;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;

    @PostMapping("/allergy-check")
    public ApiResponse<List<AllergySubstitutionResponseDTO>> checkRecipeAllergy(
            @RequestBody RecipeAllergyCheckRequestDTO request,
            @AuthenticationPrincipal(expression = "username") String userId
    ) {
        Long loginUserId = Long.parseLong(userId);

        // 1. 로그인 사용자 → 가족 프로필 조회
        FamilyMemberProfile profile =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));

        // 2. 가족방 기준으로 판단
        Long familyRoomId = profile.getFamilyRoom().getId();

        // 3. 대체 규칙 생성 (AI 입력용)
        Map<Allergen, List<AllergenAlternative>> rules =
                allergySubstitutionService.generateSubstitutionRules(
                        familyRoomId,
                        request.getIngredients()
                );

        List<AllergySubstitutionResponseDTO> response =
                AllergySubstitutionConverter.toDtoList(rules);

        return ApiResponse.onSuccess(
                AllergySuccessCode.RECIPE_ALLERGY_CHECK_OK,
                response
        );
    }
}


