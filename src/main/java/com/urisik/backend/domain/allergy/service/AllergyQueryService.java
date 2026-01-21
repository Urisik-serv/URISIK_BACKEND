package com.urisik.backend.domain.allergy.service;

import com.urisik.backend.domain.allergy.converter.AllergyConverter;
import com.urisik.backend.domain.allergy.dto.res.AllergyResponseDTO;
import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AllergyQueryService {

    private final MemberAllergyRepository memberAllergyRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;

    public List<AllergyResponseDTO> getMyAllergies(Long userId) {

        // userId로 FamilyMemberProfileId를 조회하는 로직을 추가했습니다.코드 위치는 임의로 정했으니 옮기시면 됩니다.

        FamilyMemberProfile profile = familyMemberProfileRepository.findByMember_Id(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));

        //

        List<MemberAllergy> memberAllergies =
                memberAllergyRepository.findByFamilyMemberProfile_Id(profile.getId());

        return AllergyConverter.toDtoList(
                memberAllergies.stream()
                        .map(MemberAllergy::getAllergen)
                        .toList()
        );
    }

}