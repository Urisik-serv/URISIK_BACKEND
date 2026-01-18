package com.urisik.backend.domain.allergy.repository;

import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberAllergyRepository extends JpaRepository<MemberAllergy, Long> {

    List<MemberAllergy> findByMemberId(Long memberId);

}
