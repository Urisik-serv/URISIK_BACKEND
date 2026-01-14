package com.urisik.backend.domain.familyroom.repository;

import com.urisik.backend.domain.familyroom.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    boolean existsByFamilyRoomIdAndMemberId(Long familyRoomId, Long memberId);
}
