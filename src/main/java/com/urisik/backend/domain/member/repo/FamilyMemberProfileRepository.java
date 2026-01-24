package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.enums.FamilyRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyMemberProfileRepository extends JpaRepository<FamilyMemberProfile, Long> {

    Optional<FamilyMemberProfile> findByMember_Id(Long memberId);
    List<FamilyMemberProfile> findAllByFamilyRoom_Id(Long FamilyRoomId);

    Optional<FamilyMemberProfile> findByFamilyRoom_IdAndMember_Id(Long familyRoomId, Long memberId);


    boolean existsByFamilyRoom_IdAndFamilyRoleAndIdNot(
            Long familyRoomId,
            FamilyRole role,
            Long excludeProfileId
    );
}
