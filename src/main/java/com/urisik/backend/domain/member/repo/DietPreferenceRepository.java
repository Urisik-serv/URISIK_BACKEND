package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.DietPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DietPreferenceRepository extends JpaRepository<DietPreference, Long> {
    List<DietPreference> findAllByFamilyMemberProfile_Id(Long profileId);
    void deleteAllByFamilyMemberProfile_Id(Long profileId);
}