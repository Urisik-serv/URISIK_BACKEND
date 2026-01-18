package com.urisik.backend.domain.allergy.service;

import com.urisik.backend.domain.allergy.converter.AllergyConverter;
import com.urisik.backend.domain.allergy.dto.res.AllergyResponseDTO;
import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AllergyQueryService {

    private final MemberAllergyRepository memberAllergyRepository;

    public List<AllergyResponseDTO> getMyAllergies(Long memberId) {

        List<MemberAllergy> memberAllergies =
                memberAllergyRepository.findByMemberId(memberId);

        return AllergyConverter.toDtoList(
                memberAllergies.stream()
                        .map(MemberAllergy::getAllergen)
                        .toList()
        );
    }

}