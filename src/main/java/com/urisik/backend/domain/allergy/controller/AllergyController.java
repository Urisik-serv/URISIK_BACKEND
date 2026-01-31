package com.urisik.backend.domain.allergy.controller;

import com.urisik.backend.domain.allergy.dto.res.AllergyResponseDTO;
import com.urisik.backend.domain.allergy.enums.AllergySuccessCode;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import com.urisik.backend.domain.allergy.service.AllergyQueryService;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Allergy", description = "개인 알레르기 관련 API")
public class AllergyController {

    private final AllergyQueryService allergyQueryService;

    @GetMapping("/{memberId}/allergies")
    @Operation(summary = "사용자 알레르기 조회 API", description = "사용자의 알레르기를 조회하는 api 입니다. ")
    public ApiResponse<List<AllergyResponseDTO>> getUserAllergies(
            @PathVariable Long memberId,
            @AuthenticationPrincipal Long loginUserId
    ) {

        if (!loginUserId.equals(memberId)) {
            throw new GeneralException(
                    GeneralErrorCode.FORBIDDEN,
                    "다른 사용자의 알레르기 정보에 접근할 수 없습니다."
            );
        }

        List<AllergyResponseDTO> result =
                allergyQueryService.getMyAllergies(memberId);

        return ApiResponse.onSuccess(
                AllergySuccessCode.ALLERGY_LIST_OK,
                result
        );
    }

}

