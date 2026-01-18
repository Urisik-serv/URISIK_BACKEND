package com.urisik.backend.domain.allergy.controller;

import com.urisik.backend.domain.allergy.dto.AllergyResponseDTO;
import com.urisik.backend.domain.allergy.enums.AllergySuccessCode;
import com.urisik.backend.domain.allergy.service.AllergyQueryService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AllergyController {

    private final AllergyQueryService allergyQueryService;

    @GetMapping("/{userId}/allergies")
    public ApiResponse<List<AllergyResponseDTO>> getUserAllergies(
            @PathVariable Long userId,
            @AuthenticationPrincipal Long loginUserId
    ) {

        if (!loginUserId.equals(userId)) {
            throw new GeneralException(
                    GeneralErrorCode.FORBIDDEN,
                    "다른 사용자의 알레르기 정보에 접근할 수 없습니다."
            );
        }

        List<AllergyResponseDTO> result =
                allergyQueryService.getMyAllergies(userId);

        return ApiResponse.onSuccess(
                AllergySuccessCode.ALLERGY_LIST_OK,
                result
        );
    }

}

