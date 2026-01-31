package com.urisik.backend.domain.allergy.service;

import com.urisik.backend.domain.allergy.converter.AllergyConverter;
import com.urisik.backend.domain.allergy.dto.res.AllergyResponseDTO;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FamilyAllergyQueryService {

    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final MemberAllergyRepository memberAllergyRepository;

    public List<AllergyResponseDTO> getFamilyAllergies(Long loginUserId) {

        // 1️. 로그인 사용자 → 가족 프로필 조회
        FamilyMemberProfile profile =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() -> new GeneralException(
                                GeneralErrorCode.NOT_FOUND,
                                "가족 프로필을 찾을 수 없습니다."
                        ));

        Long familyRoomId = profile.getFamilyRoom().getId();

        // 2️. 가족방 전체 알레르기 조회 (중복 제거)
        List<Allergen> allergens =
                memberAllergyRepository.findDistinctAllergensByFamilyRoomId(familyRoomId);

        // 3️. DTO 변환
        return AllergyConverter.toDtoList(allergens);
    }

}
