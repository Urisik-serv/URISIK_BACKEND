package com.urisik.backend.domain.member.repo;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyMemberProfileRepository extends JpaRepository<FamilyMemberProfile, Long> {

    Optional<FamilyMemberProfile> findByMember_Id(Long memberId);
    List<FamilyMemberProfile> findAllByFamilyRoom_Id(Long FamilyRoomId);

}
