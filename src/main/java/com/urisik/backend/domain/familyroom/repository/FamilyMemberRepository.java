package com.urisik.backend.domain.familyroom.repository;

import com.urisik.backend.domain.familyroom.entity.FamilyMember;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.familyroom.enums.FamilyRole;
import com.urisik.backend.domain.familyroom.enums.FamilyStatus;
import com.urisik.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    boolean existsByFamilyRoomAndMember(FamilyRoom familyRoom, Member member);


    //가족방 내의 역할정보들 가져오기
    List<FamilyMember> findAllByFamilyRoom_IdAndFamilyRoleAndStatus(
            Long familyRoomId,
            FamilyRole familyRole,
            FamilyStatus status  // 예: active
    );
}
