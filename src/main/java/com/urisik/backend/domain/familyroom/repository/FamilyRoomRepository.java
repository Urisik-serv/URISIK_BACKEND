package com.urisik.backend.domain.familyroom.repository;

import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FamilyRoomRepository extends JpaRepository<FamilyRoom, Long> {

    @Query("select fr.id from FamilyRoom fr join fr.members m where m.id = :memberId")
    Optional<Long> findIdByMembers_Id(Long memberId);
}
