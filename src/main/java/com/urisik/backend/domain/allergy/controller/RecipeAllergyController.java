package com.urisik.backend.domain.allergy.controller;

import com.urisik.backend.domain.allergy.converter.AllergySubstitutionConverter;
import com.urisik.backend.domain.allergy.dto.req.RecipeAllergyCheckRequestDTO;
import com.urisik.backend.domain.allergy.dto.res.AllergySubstitutionResponseDTO;
import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.enums.AllergySuccessCode;
import com.urisik.backend.domain.allergy.service.AllergySubstitutionService;
import com.urisik.backend.global.apiPayload.ApiResponse;
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

    @PostMapping("/allergy-check")
    public ApiResponse<List<AllergySubstitutionResponseDTO>> checkRecipeAllergy(
            @RequestBody RecipeAllergyCheckRequestDTO request,
            @AuthenticationPrincipal(expression = "userName") String userId
    ) {
        Long loginUserId = Long.parseLong(userId);

        Map<Allergen, List<AllergenAlternative>> result =
                allergySubstitutionService.checkAndMapSubstitutions(
                        loginUserId,
                        request.getIngredients()
                );

        List<AllergySubstitutionResponseDTO> response =
                AllergySubstitutionConverter.toDtoList(result);

        return ApiResponse.onSuccess(
                AllergySuccessCode.RECIPE_ALLERGY_CHECK_OK,
                response
        );
    }

}

