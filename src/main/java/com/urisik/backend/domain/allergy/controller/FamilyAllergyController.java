package com.urisik.backend.domain.allergy.controller;

import com.urisik.backend.domain.allergy.dto.res.AllergyResponseDTO;
import com.urisik.backend.domain.allergy.enums.AllergySuccessCode;
import com.urisik.backend.domain.allergy.service.FamilyAllergyQueryService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/allergy")
@Tag(name = "Allergy", description = "알레르기 관련 API")
public class FamilyAllergyController {

    private final FamilyAllergyQueryService familyAllergyQueryService;

    @GetMapping("/families/allergies")
    @Operation(
            summary = "가족방 전체 알레르기 조회 API",
            description = "로그인한 사용자가 속한 가족방 구성원들의 알레르기를 종합 조회합니다."
    )
    public ApiResponse<List<AllergyResponseDTO>> getFamilyAllergies(
            @AuthenticationPrincipal Long loginUserId
    ) {

        List<AllergyResponseDTO> result =
                familyAllergyQueryService.getFamilyAllergies(loginUserId);

        return ApiResponse.onSuccess(
                AllergySuccessCode.FAMILY_ALLERGY_LIST_OK,
                result
        );
    }

}
