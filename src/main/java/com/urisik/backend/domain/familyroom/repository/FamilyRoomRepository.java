package com.urisik.backend.domain.familyroom.repository;

import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRoomRepository extends JpaRepository<FamilyRoom, Long> {

    // 맴버가 속한 familyroom 가져오기
    Optional<FamilyRoom> findByMembers_Id(Long memberId);
}
